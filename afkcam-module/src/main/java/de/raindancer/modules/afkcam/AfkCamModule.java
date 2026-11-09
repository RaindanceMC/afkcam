package de.raindancer.modules.afkcam;

import de.raindancer.core.content.pack.PackContribution;
import de.raindancer.core.content.pack.ResourcePacks;
import de.raindancer.core.data.settings.SettingsStore;
import de.raindancer.core.platform.util.Scheduling;
import de.raindancer.core.social.presence.Away;
import de.raindancer.core.ui.camera.PlayerCamera;
import de.raindancer.modules.afkcam.listener.AfkCamListener;
import de.raindancer.modules.afkcam.music.MusicFolder;
import de.raindancer.modules.afkcam.music.Track;
import de.raindancer.modules.afkcam.service.AfkCamService;
import de.raindancer.modules.afkcam.util.PermissionNodes;
import de.raindancer.modules.api.FlexModule;
import de.raindancer.modules.api.ModuleCommand;
import de.raindancer.modules.api.ModuleContext;
import de.raindancer.modules.api.ModuleInfo;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Calm, cinematic still angles of whoever is away, on their own screen only, until they press a key. Who is away
 * is Core's {@code Away}, answered by the essentials module; the camera itself is Core's {@link PlayerCamera}.
 */
public final class AfkCamModule implements FlexModule {

    private static final String PACK_OWNER = "AfkCam";
    private static final String PACK_NAME = "music";

    private static final ModuleInfo INFO = ModuleInfo.of("afkcam", "AFK Cam", "0.3.3")
            .describedAs("Calm cinematic camera angles of you while you are away, until you press a key")
            .by("Raindancer118");

    private volatile AfkCamService cams;
    private volatile Server server;

    @Override
    public ModuleInfo info() {
        return INFO;
    }

    @Override
    public void enable(ModuleContext context) {
        SettingsStore<AfkCamSettings> settings = context.settings(AfkCamSettings.class, AfkCamSettings.DEFAULTS);
        context.core().messages().defineFrom(AfkCamModule.class.getResourceAsStream("messages.yml"),
                context.chat().brand()::chatPrefix);
        context.core().messages().seriousFrom(AfkCamModule.class.getResourceAsStream("messages-serious.yml"));
        PermissionNodes.register(context.plugin().getServer());

        server = context.plugin().getServer();
        ResourcePacks packs = context.core().resourcePacks();
        List<Track> tracks = music(context, packs);
        cams = new AfkCamService(context.plugin(), PlayerCamera.of(server), settings::current,
                context.core().actionBars(), context.core().messages(), System::currentTimeMillis, () -> tracks,
                packs::isWearing);
        context.listener(new AfkCamListener(context.plugin(), cams));
        for (Player online : server.getOnlinePlayers()) {
            cams.track(online);
        }
        AfkCamCommands.ready(new AfkCamServices(context.core().messages(), cams));

        if (!cams.works()) {
            context.log().warn("AFK cam is up, but this server cannot move a player's camera — nobody's will.");
            return;
        }
        // Asked once the server is up: essentials may well enable after this module, and Away is asked live anyway.
        Scheduling.globalLater(context.plugin(), 1, () -> {
            if (!Away.isKnown()) {
                context.log().info("AFK cam is up, but nothing says who is away (the essentials module) — only "
                        + "/afkcam preview.");
            } else {
                context.log().info("AFK cam is up: a camera starts {} s after somebody is away.",
                        settings.current().startAfterSeconds());
            }
        });
    }

    /** The owner's tracks from {@code plugins/RainsAfkCam/music}, offered to the server's resource pack. */
    private static List<Track> music(ModuleContext context, ResourcePacks packs) {
        Path data = context.plugin().getDataFolder().toPath();
        Path pack = data.resolve("pack").resolve(PACK_NAME);
        try {
            List<Track> tracks = MusicFolder.build(data.resolve("music"), pack);
            if (tracks.isEmpty()) {
                return tracks;
            }
            packs.contribute(PackContribution.of(PACK_OWNER, PACK_NAME, pack).describedAs("Music for the AFK camera"));
            context.closeWith(() -> packs.withdraw(PACK_OWNER.toLowerCase(java.util.Locale.ROOT) + ":" + PACK_NAME));
            if (packs.current().isPresent()) {
                Scheduling.async(context.plugin(), () -> packs.rebuild().ifPresent(built -> packs.sendToAll(
                        context.plugin().getServer().getOnlinePlayers().stream().map(Player::getUniqueId).toList())));
            }
            context.log().info("AFK music: {} track(s) — {}.", tracks.size(),
                    String.join(", ", tracks.stream().map(Track::name).toList()));
            return tracks;
        } catch (IOException failure) {
            context.log().warn("The AFK music could not be read ({}); the camera plays the game's menu music.",
                    failure.getMessage());
            return List.of();
        }
    }

    @Override
    public List<ModuleCommand> commands() {
        return AfkCamCommands.declared();
    }

    @Override
    public void disable() {
        AfkCamCommands.stopped();
        AfkCamService running = cams;
        if (running != null && server != null) {
            running.stopAll(server.getOnlinePlayers());
        }
        cams = null;
    }
}
