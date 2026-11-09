package de.raindancer.modules.afkcam.service;

import com.destroystokyo.paper.ClientOption;
import de.raindancer.core.platform.util.Scheduling;
import de.raindancer.core.social.presence.Away;
import de.raindancer.core.ui.actionbar.ActionBarPriority;
import de.raindancer.core.ui.actionbar.ActionBars;
import de.raindancer.core.ui.camera.PlayerCamera;
import de.raindancer.core.ui.messages.Messages;
import de.raindancer.core.ui.profile.PlayerSwitch;
import de.raindancer.modules.afkcam.AfkCamSettings;
import de.raindancer.modules.afkcam.model.AwayWatch;
import de.raindancer.modules.afkcam.model.Framing;
import de.raindancer.modules.afkcam.model.Lift;
import de.raindancer.modules.afkcam.model.Point;
import de.raindancer.modules.afkcam.model.Pose;
import de.raindancer.modules.afkcam.model.Reel;
import de.raindancer.modules.afkcam.rules.CamRule;
import de.raindancer.modules.afkcam.rules.CamRule.Moment;
import de.raindancer.modules.afkcam.rules.CamRule.Verdict;
import de.raindancer.modules.afkcam.util.PermissionNodes;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.title.Title;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * The cameras: one ticking task per online player that notices them going away, and for whoever has a camera, an
 * invisible display only they can see, moved along the {@link Reel} every tick and looked through with
 * {@link PlayerCamera} — plus a mannequin in their skin and gear where they stand, also only for them: the game never
 * draws a player's own body to themselves unless they are the camera, so without it the camera circles nobody.
 *
 * <p>Everything a camera changes is only on the player's own screen — the camera, their sky, a sound, some
 * particles — and all of it is put back by {@link #stop}. Nothing is stored: a crash or a quit leaves a client that
 * sees through its own eyes again on the next join, and the display is never saved with the chunk.
 */
public final class AfkCamService {

    private static final String OWNER = "afkcam";
    private static final long GOLDEN_HOUR = 12_600;
    private static final double SECONDS_PER_TICK = 0.05;
    private static final double BLEND_SECONDS = 4;
    /** Heights tried above a blocked shot: 0, 2, 4, 6 and 8 blocks. */
    private static final int LIFTS = 5;

    public enum Reason { BACK, KEY, HURT, MOVED, QUIT, OFF }

    private final Plugin plugin;
    private final PlayerCamera camera;
    private final Supplier<AfkCamSettings> settings;
    private final ActionBars bars;
    private final Messages messages;
    private final LongSupplier clock;
    private final AwayWatch watch = new AwayWatch();
    private final CamRule rule = new CamRule();
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledTask> tasks = new ConcurrentHashMap<>();

    private static final class Session {
        final ItemDisplay display;
        final Mannequin standIn;
        final Reel reel;
        final Framing framing = new Framing(0.3, 1.5);
        final Lift lift = new Lift(2, 1.5);
        double liftTarget;
        final long startedAt;
        final boolean preview;
        final boolean skySet;
        final String music;
        int ticks;

        Session(ItemDisplay display, Mannequin standIn, Reel reel, long startedAt, boolean preview, boolean skySet,
                String music) {
            this.display = display;
            this.standIn = standIn;
            this.reel = reel;
            this.startedAt = startedAt;
            this.preview = preview;
            this.skySet = skySet;
            this.music = music;
        }
    }

    public AfkCamService(Plugin plugin, PlayerCamera camera, Supplier<AfkCamSettings> settings, ActionBars bars,
                         Messages messages, LongSupplier clock) {
        this.plugin = plugin;
        this.camera = camera;
        this.settings = settings;
        this.bars = bars;
        this.messages = messages;
        this.clock = clock;
    }

    public PlayerSwitch wanted() {
        return new PlayerSwitch(OWNER, "camera", settings.get().onByDefault());
    }

    public boolean works() {
        return camera.works();
    }

    public boolean running(UUID who) {
        return sessions.containsKey(who);
    }

    /** Starts watching {@code player}; again after a respawn, when the old task went with the old body. */
    public void track(Player player) {
        UUID id = player.getUniqueId();
        ScheduledTask old = tasks.remove(id);
        if (old != null) {
            old.cancel();
        }
        tasks.put(id, Scheduling.entityTimer(plugin, player, 1, 1, task -> tick(player),
                () -> tasks.remove(id)));
    }

    public void untrack(Player player) {
        stop(player, Reason.QUIT);
        ScheduledTask task = tasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        watch.forget(player.getUniqueId());
    }

    public void hurt(Player player) {
        watch.hurt(player.getUniqueId(), clock.getAsLong());
        stop(player, Reason.HURT);
    }

    /** {@code /afkcam preview}: right now, away or not, until a key. */
    public Verdict preview(Player player) {
        if (running(player.getUniqueId())) {
            return Verdict.START;
        }
        Verdict verdict = rule.decide(moment(player, true), 0, settings.get().calmSeconds());
        if (verdict == Verdict.START) {
            start(player, true);
        }
        return verdict;
    }

    private Moment moment(Player player, boolean preview) {
        UUID id = player.getUniqueId();
        long now = clock.getAsLong();
        AfkCamSettings s = settings.get();
        return new Moment(s.enabled(), camera.works(), player.hasPermission(PermissionNodes.USE),
                wanted().isOn(player), Away.isAway(id), watch.awaySeconds(id, now), watch.sinceHurtSeconds(id, now),
                player.getGameMode() == GameMode.SPECTATOR, watch.held(id), preview);
    }

    private void tick(Player player) {
        if (!player.isOnline()) {
            return;
        }
        UUID id = player.getUniqueId();
        watch.observe(id, Away.isAway(id), clock.getAsLong());
        Session session = sessions.get(id);
        if (session == null) {
            if (player.getTicksLived() % 20 == 0 && !player.isDead()) {
                AfkCamSettings s = settings.get();
                if (rule.decide(moment(player, false), s.startAfterSeconds(), s.calmSeconds()) == Verdict.START) {
                    start(player, false);
                }
            }
            return;
        }
        session.ticks++;
        if (session.ticks % 10 == 0 && !rule.keeps(moment(player, session.preview))) {
            stop(player, Reason.BACK);
            return;
        }
        if (!session.display.isValid() || !session.standIn.isValid()
                || session.display.getWorld() != player.getWorld()) {
            stop(player, Reason.MOVED);
            return;
        }
        frame(player, session, false);
        follow(player, session);
        if (session.ticks == 3 || session.ticks % 40 == 0) {
            // Again now and then: a client that lost track of the display (a lag spike, a dimension's loading
            // screen) would otherwise keep looking through nothing.
            camera.look(player, session.display);
        }
        if (session.ticks % 20 == 0) {
            showBar(player, session);
        }
        if (session.ticks % 8 == 0 && settings.get().ambience()) {
            sparkle(player);
        }
    }

    private void start(Player player, boolean preview) {
        AfkCamSettings s = settings.get();
        Reel reel = Reel.standard(s.radius(), s.height(), s.secondsPerTurn(), s.shotSeconds(), BLEND_SECONDS);
        Location head = player.getEyeLocation();
        ItemDisplay display = head.getWorld().spawn(head, ItemDisplay.class, d -> {
            d.setPersistent(false);
            d.setVisibleByDefault(false);
            d.setTeleportDuration(2);
            d.setSilent(true);
        });
        player.showEntity(plugin, display);
        Mannequin standIn = head.getWorld().spawn(player.getLocation(), Mannequin.class, m -> {
            m.setPersistent(false);
            m.setVisibleByDefault(false);
            m.setProfile(ResolvableProfile.resolvableProfile(player.getPlayerProfile()));
            m.setSkinParts(player.getClientOption(ClientOption.SKIN_PARTS));
            m.setMainHand(player.getMainHand());
            m.setDescription(null);
            m.setImmovable(true);
            m.setGravity(false);
            m.setInvulnerable(true);
            m.setCollidable(false);
            m.setSilent(true);
            dress(m, player);
        });
        player.showEntity(plugin, standIn);
        boolean skySet = false;
        if (s.goldenHour() && player.isPlayerTimeRelative() && player.getPlayerTimeOffset() == 0) {
            player.setPlayerTime(GOLDEN_HOUR, false);
            skySet = true;
        }
        String music = s.music() == null ? "" : s.music().trim();
        Session session = new Session(display, standIn, reel, clock.getAsLong(), preview, skySet, music);
        sessions.put(player.getUniqueId(), session);
        frame(player, session, true);
        player.showTitle(Title.title(messages.get("afkcam.title"),
                messages.get(preview ? "afkcam.subtitle-preview" : "afkcam.subtitle"),
                Title.Times.times(Duration.ofMillis(1500), Duration.ofSeconds(4), Duration.ofMillis(1500))));
        if (!music.isEmpty() && Key.parseable(music)) {
            player.playSound(Sound.sound(Key.key(music), Sound.Source.MUSIC, 0.6f, 1f), Sound.Emitter.self());
        }
        showBar(player, session);
    }

    /** Puts back everything a camera changed. Safe to call for somebody who has none. */
    public void stop(Player player, Reason reason) {
        Session session = sessions.remove(player.getUniqueId());
        if (session == null) {
            return;
        }
        if (player.isOnline()) {
            camera.reset(player);
            bars.clear(player.getUniqueId(), OWNER);
            if (session.skySet) {
                player.resetPlayerTime();
            }
            if (!session.music.isEmpty() && Key.parseable(session.music)) {
                player.stopSound(SoundStop.namedOnSource(Key.key(session.music), Sound.Source.MUSIC));
            }
            if (reason == Reason.KEY && Away.isAway(player.getUniqueId())) {
                watch.hold(player.getUniqueId());
            }
            if (reason == Reason.KEY || reason == Reason.BACK) {
                bars.show(player.getUniqueId(), OWNER, messages.get("afkcam.back"), Duration.ofSeconds(2),
                        ActionBarPriority.LOW);
            }
        }
        Scheduling.entity(plugin, session.display, session.display::remove);
        Scheduling.entity(plugin, session.standIn, session.standIn::remove);
    }

    public void stopAll(Iterable<? extends Player> online) {
        for (Player player : online) {
            stop(player, Reason.OFF);
        }
        for (ScheduledTask task : List.copyOf(tasks.values())) {
            task.cancel();
        }
        tasks.clear();
    }

    private void frame(Player player, Session session, boolean first) {
        Location eye = player.getEyeLocation();
        Point head = new Point(eye.getX(), eye.getY(), eye.getZ());
        double seconds = (clock.getAsLong() - session.startedAt) / 1000.0;
        Point shot = session.reel.position(head, seconds);
        if (first || session.ticks % 5 == 0) {
            double[] shares = new double[LIFTS];
            for (int i = 0; i < LIFTS; i++) {
                Point raised = shot.plus(new Point(0, session.lift.blocks(i), 0));
                double length = raised.minus(head).length();
                shares[i] = Math.min(1, clear(eye, head, raised) / length);
            }
            session.liftTarget = session.lift.blocks(Lift.choose(shares));
        }
        Point wanted = shot.plus(new Point(0, session.lift.ease(session.liftTarget, first ? 1000 : SECONDS_PER_TICK), 0));
        Point at = session.framing.frame(head, wanted, clear(eye, head, wanted), SECONDS_PER_TICK);
        Pose pose = Pose.looking(at, head);
        Location to = new Location(eye.getWorld(), at.x(), at.y(), at.z(), pose.yaw(), pose.pitch());
        if (first) {
            session.display.teleport(to);
        } else {
            session.display.teleportAsync(to);
        }
    }

    /** How far from the head towards {@code to} is free of solid blocks; infinite when nothing is hit. */
    private static double clear(Location eye, Point head, Point to) {
        Point offset = to.minus(head);
        double length = offset.length();
        if (length == 0) {
            return Double.POSITIVE_INFINITY;
        }
        RayTraceResult hit = eye.getWorld().rayTraceBlocks(eye, new Vector(offset.x(), offset.y(), offset.z()),
                length + 0.5, FluidCollisionMode.NEVER, true);
        return hit == null ? Double.POSITIVE_INFINITY : hit.getHitPosition().distance(eye.toVector());
    }

    /** The stand-in stays where the player is — pushed by water, in a minecart — and wears what they wear. */
    private static void follow(Player player, Session session) {
        Location at = player.getLocation();
        Location was = session.standIn.getLocation();
        if (at.distanceSquared(was) > 1.0E-4 || Math.abs(at.getYaw() - was.getYaw()) > 0.5f) {
            session.standIn.teleportAsync(at);
        }
        if (session.ticks % 20 == 0) {
            dress(session.standIn, player);
        }
    }

    private static void dress(Mannequin standIn, Player player) {
        EntityEquipment worn = player.getEquipment();
        EntityEquipment copy = standIn.getEquipment();
        copy.setArmorContents(worn.getArmorContents());
        copy.setItemInMainHand(worn.getItemInMainHand());
        copy.setItemInOffHand(worn.getItemInOffHand());
    }

    private void showBar(Player player, Session session) {
        long minutes = (clock.getAsLong() - session.startedAt) / 60_000;
        bars.show(player.getUniqueId(), OWNER, session.preview ? messages.get("afkcam.bar-preview")
                        : messages.get("afkcam.bar", "minutes", minutes),
                ActionBars.UNTIL_CLEARED, ActionBarPriority.LOW);
    }

    private static void sparkle(Player player) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Location around = player.getEyeLocation().add(random.nextDouble(-4, 4), random.nextDouble(-1, 3),
                random.nextDouble(-4, 4));
        player.spawnParticle(Particle.END_ROD, around, 0, random.nextDouble(-0.02, 0.02), 0.03,
                random.nextDouble(-0.02, 0.02), 1);
    }
}
