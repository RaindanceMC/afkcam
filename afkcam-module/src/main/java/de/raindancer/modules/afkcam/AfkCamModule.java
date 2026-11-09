package de.raindancer.modules.afkcam;

import de.raindancer.core.data.settings.SettingsStore;
import de.raindancer.core.social.presence.Away;
import de.raindancer.core.ui.camera.PlayerCamera;
import de.raindancer.modules.afkcam.listener.AfkCamListener;
import de.raindancer.modules.afkcam.service.AfkCamService;
import de.raindancer.modules.afkcam.util.PermissionNodes;
import de.raindancer.modules.api.FlexModule;
import de.raindancer.modules.api.ModuleCommand;
import de.raindancer.modules.api.ModuleContext;
import de.raindancer.modules.api.ModuleInfo;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * A slow, cinematic camera around whoever is away, on their own screen only, until they press a key. Who is away
 * is Core's {@code Away}, answered by the essentials module; the camera itself is Core's {@link PlayerCamera}.
 */
public final class AfkCamModule implements FlexModule {

    private static final ModuleInfo INFO = ModuleInfo.of("afkcam", "AFK Cam", "0.2.0")
            .describedAs("A slow cinematic camera around you while you are away, until you press a key")
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
        cams = new AfkCamService(context.plugin(), PlayerCamera.of(server), settings::current,
                context.core().actionBars(), context.core().messages(), System::currentTimeMillis);
        context.listener(new AfkCamListener(context.plugin(), cams));
        for (Player online : server.getOnlinePlayers()) {
            cams.track(online);
        }
        AfkCamCommands.ready(new AfkCamServices(context.core().messages(), cams));

        if (!cams.works()) {
            context.log().warn("AFK cam is up, but this server cannot move a player's camera — nobody's will.");
        } else if (!Away.isKnown()) {
            // Not an error: /afkcam preview works, and the camera starts by itself once something says who is away.
            context.log().info("AFK cam is up, but nothing says who is away (the essentials module) — only /afkcam preview.");
        } else {
            context.log().info("AFK cam is up: a camera starts {} s after somebody is away.",
                    settings.current().startAfterSeconds());
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
