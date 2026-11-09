package de.raindancer.modules.afkcam;

import de.raindancer.modules.afkcam.command.AfkCamCommand;
import de.raindancer.modules.api.ModuleCommand;

import java.util.List;

/**
 * Declared at bootstrap, filled in when the module starts — Paper registers commands before any
 * plugin is enabled, so the command points at {@link #require} until then.
 */
public final class AfkCamCommands {

    private static volatile AfkCamServices services;

    private AfkCamCommands() {
    }

    public static List<ModuleCommand> declared() {
        return List.of(ModuleCommand.of("afkcam", "Switches your AFK camera, or shows it now",
                        new AfkCamCommand(AfkCamCommands::require))
                .aliased("afkcamera")
                .taking("(nothing) — switch it", "on | off | status", "preview — see it right now")
                .needing("rainsafkcam.use"));
    }

    static void ready(AfkCamServices live) {
        services = live;
    }

    static void stopped() {
        services = null;
    }

    private static AfkCamServices require() {
        AfkCamServices live = services;
        if (live == null) {
            throw new IllegalStateException("the afkcam module is not running");
        }
        return live;
    }
}
