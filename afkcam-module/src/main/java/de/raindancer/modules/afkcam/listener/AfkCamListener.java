package de.raindancer.modules.afkcam.listener;

import de.raindancer.modules.afkcam.service.AfkCamService;
import de.raindancer.modules.afkcam.service.AfkCamService.Reason;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

import de.raindancer.core.platform.util.Scheduling;

/**
 * What ends a camera at once, without waiting for the next look at whether they are still away: a key, a click,
 * the hotbar, being hurt, being moved somewhere else.
 *
 * <p>A key is the client's own input packet, which it still sends while looking through the display — it just
 * does not walk until it sees through its own eyes again.
 */
public final class AfkCamListener implements Listener {

    private final Plugin plugin;
    private final AfkCamService cams;

    public AfkCamListener(Plugin plugin, AfkCamService cams) {
        this.plugin = plugin;
        this.cams = cams;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        cams.track(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Scheduling.entityLater(plugin, player, 1, () -> cams.track(player));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cams.untrack(event.getPlayer());
    }

    @EventHandler
    public void onInput(PlayerInputEvent event) {
        var input = event.getInput();
        if (input.isForward() || input.isBackward() || input.isLeft() || input.isRight() || input.isJump()
                || input.isSneak() || input.isSprint()) {
            back(event.getPlayer());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        back(event.getPlayer());
    }

    @EventHandler
    public void onSwing(PlayerAnimationEvent event) {
        back(event.getPlayer());
    }

    @EventHandler
    public void onHotbar(PlayerItemHeldEvent event) {
        back(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHurt(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && event.getFinalDamage() > 0) {
            cams.hurt(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        cams.stop(event.getPlayer(), Reason.MOVED);
    }

    @EventHandler
    public void onWorld(PlayerChangedWorldEvent event) {
        cams.stop(event.getPlayer(), Reason.MOVED);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        cams.stop(event.getPlayer(), Reason.MOVED);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGameMode(PlayerGameModeChangeEvent event) {
        if (event.getNewGameMode() == GameMode.SPECTATOR) {
            cams.stop(event.getPlayer(), Reason.MOVED);
        }
    }

    private void back(Player player) {
        if (cams.running(player.getUniqueId())) {
            cams.stop(player, Reason.KEY);
        }
    }
}
