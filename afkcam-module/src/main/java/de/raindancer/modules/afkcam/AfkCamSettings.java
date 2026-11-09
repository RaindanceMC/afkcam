package de.raindancer.modules.afkcam;

import de.raindancer.core.data.settings.Describe;
import de.raindancer.core.data.settings.In;
import de.raindancer.core.data.settings.Key;
import de.raindancer.core.data.settings.Range;
import de.raindancer.core.data.settings.Settings;
import de.raindancer.core.data.settings.Title;
import de.raindancer.core.data.settings.Topic;
import org.bukkit.Material;

/** What an owner decides about the AFK camera. The schema of the file and of the /settings page. */
@Settings(id = "afkcam", topics = {
        @Topic(path = "afkcam", title = "AFK camera", icon = Material.SPYGLASS),
})
public record AfkCamSettings(

        @In("afkcam") @Title("AFK camera on")
        @Describe("Whether players who are away see themselves from a slow camera circling them. Off, nobody's "
                + "camera moves and /afkcam preview is refused.")
        @Key("enabled")
        boolean enabled,

        @In("afkcam") @Title("On for somebody who never chose")
        @Describe("Whether the camera starts for a player who never typed /afkcam. Anybody who chose keeps what "
                + "they chose.")
        @Key("on-by-default")
        boolean onByDefault,

        @In("afkcam") @Title("Start after (seconds away)") @Range(min = 0, max = 600)
        @Describe("How long somebody has to count as away before their camera starts. Being away itself is "
                + "decided by the essentials module's AFK timeout, so this comes on top of that.")
        @Key("start-after-seconds")
        int startAfterSeconds,

        @In("afkcam") @Title("Not after being hurt (seconds)") @Range(min = 0, max = 600)
        @Describe("Being hurt ends the camera. It does not start again until the player has not been hurt for "
                + "this long, so a mob farm that nicks you now and then does not flick it on and off.")
        @Key("calm-seconds")
        int calmSeconds,

        @In("afkcam") @Title("Distance (blocks)") @Range(min = 2, max = 16)
        @Describe("How far from the player the close shot circles. The other shots are worked out from it. "
                + "The camera always stays on the player's side of a wall.")
        @Key("radius")
        int radius,

        @In("afkcam") @Title("Height (blocks)") @Range(min = -2, max = 10)
        @Describe("How far above the player's eyes the close shot circles.")
        @Key("height")
        int height,

        @In("afkcam") @Title("One turn takes (seconds)") @Range(min = 20, max = 600)
        @Describe("How long the close shot takes to go once round the player. Slower looks calmer.")
        @Key("seconds-per-turn")
        int secondsPerTurn,

        @In("afkcam") @Title("One shot lasts (seconds)") @Range(min = 5, max = 300)
        @Describe("How long each shot lasts before the camera glides into the next one: close, wide from high "
                + "up, low looking up, and from straight above.")
        @Key("shot-seconds")
        int shotSeconds,

        @In("afkcam") @Title("Golden hour")
        @Describe("Whether the sky turns to sunset for the player while their camera runs, and back when it "
                + "ends. Only their own sky; somebody who set their own time keeps it.")
        @Key("golden-hour")
        boolean goldenHour,

        @In("afkcam") @Title("Floating light")
        @Describe("Whether a few slow sparks drift around the player while their camera runs. Only they see "
                + "them.")
        @Key("ambience")
        boolean ambience,

        @In("afkcam") @Title("Music")
        @Describe("The sound played to the player when their camera starts, stopped when it ends — a key like "
                + "minecraft:music.menu. Empty plays nothing.")
        @Key("music")
        String music,

        @In("afkcam") @Title("Hide arm and HUD")
        @Describe("Whether the player's own arm, hotbar and hearts disappear while their camera runs, as on a "
                + "spectator's screen. Only their client is told it spectates; the server keeps them in their "
                + "game mode, so farms and pickup run on. Only while they stand on solid ground, never for "
                + "Bedrock players.")
        @Key("hide-hud")
        boolean hideHud) {

    public static final AfkCamSettings DEFAULTS = new AfkCamSettings(true, true, 5, 20, 6, 2, 90, 20, true, true,
            "minecraft:music.menu", true);
}
