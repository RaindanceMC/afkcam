package de.raindancer.modules.afkcam;

import de.raindancer.core.ui.preference.Preference;
import de.raindancer.modules.afkcam.util.PermissionNodes;

import java.util.function.Supplier;

/** What /preferences offers for the AFK camera: the same switch /afkcam flips. */
public final class AfkCamPreferences {

    private AfkCamPreferences() {
    }

    /** Offered to whoever may use /afkcam, and not while the server has the camera off. */
    public static Preference camera(Supplier<AfkCamServices> services, Supplier<AfkCamSettings> settings) {
        return Preference.toggle("afkcam:camera", "AFK camera", "AFK camera",
                        "Shows you from calm camera angles while you are away, until you press a key.",
                        player -> services.get().cams().wanted().isOn(player),
                        (player, on) -> {
                            AfkCamServices live = services.get();
                            live.cams().wanted().set(player, on);
                            live.messages().send(player, on ? "afkcam.switched-on" : "afkcam.switched-off");
                        })
                .shownTo(player -> settings.get().enabled() && player.hasPermission(PermissionNodes.USE));
    }
}
