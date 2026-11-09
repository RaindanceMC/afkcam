package de.raindancer.modules.afkcam.music;

import net.kyori.adventure.key.Key;

/**
 * What a camera plays: the owner's tracks ({@link #PLAYLIST}), one sound by its key, or nothing. {@code custom}
 * falls back to the game's menu music for a player without the server's pack, or when the folder is empty — their
 * client would have nothing to play.
 */
public record MusicChoice(boolean playlist, String key) {

    public static final String CUSTOM = "custom";
    public static final String FALLBACK = "minecraft:music.menu";
    public static final MusicChoice PLAYLIST = new MusicChoice(true, "");
    public static final MusicChoice NONE = new MusicChoice(false, "");

    public static MusicChoice of(String setting, boolean haveTracks, boolean wearsPack) {
        String value = setting == null ? "" : setting.trim();
        if (value.equalsIgnoreCase(CUSTOM)) {
            return haveTracks && wearsPack ? PLAYLIST : new MusicChoice(false, FALLBACK);
        }
        return !value.isEmpty() && Key.parseable(value) ? new MusicChoice(false, value) : NONE;
    }
}
