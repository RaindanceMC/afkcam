package de.raindancer.modules.afkcam.music;

import java.util.Locale;

/** One of the owner's tracks: its name in the pack and how long it plays. */
public record Track(String name, double seconds) {

    public static final String NAMESPACE = "rainsafkcam";

    /** The sound event the client plays. */
    public String sound() {
        return NAMESPACE + ":afk." + name;
    }

    /** A file name as a resource-pack name: lower case, a–z, 0–9 and underscores only. */
    public static String slug(String fileName) {
        String base = fileName.replaceFirst("(?i)\\.ogg$", "").toLowerCase(Locale.ROOT);
        return base.replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
    }
}
