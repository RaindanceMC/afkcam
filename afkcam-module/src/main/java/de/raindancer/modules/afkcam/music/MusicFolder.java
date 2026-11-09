package de.raindancer.modules.afkcam.music;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Stream;

/**
 * The owner's own AFK music: every {@code .ogg} (Vorbis) in the music folder becomes a sound in a small resource
 * pack — {@code rainsafkcam:afk.<name>}, streamed — which Core merges into the server's pack. The pack folder is
 * rebuilt from scratch each time, so a track taken out of the music folder is gone from the pack too.
 */
public final class MusicFolder {

    private MusicFolder() {
    }

    public static List<Track> build(Path music, Path pack) throws IOException {
        Files.createDirectories(music);
        deleteTree(pack);
        List<Track> tracks = new ArrayList<>();
        JsonObject sounds = new JsonObject();
        Path into = pack.resolve("assets").resolve(Track.NAMESPACE).resolve("sounds").resolve("afk");
        try (Stream<Path> files = Files.list(music)) {
            for (Path file : files.filter(f -> f.getFileName().toString().toLowerCase().endsWith(".ogg")).sorted()
                    .toList()) {
                String name = Track.slug(file.getFileName().toString());
                OptionalDouble seconds = OggLength.seconds(Files.readAllBytes(file));
                if (name.isEmpty() || seconds.isEmpty() || tracks.stream().anyMatch(t -> t.name().equals(name))) {
                    continue;
                }
                Files.createDirectories(into);
                Files.copy(file, into.resolve(name + ".ogg"));
                JsonObject sound = new JsonObject();
                sound.addProperty("name", Track.NAMESPACE + ":afk/" + name);
                sound.addProperty("stream", true);
                JsonArray list = new JsonArray();
                list.add(sound);
                JsonObject event = new JsonObject();
                event.add("sounds", list);
                sounds.add("afk." + name, event);
                tracks.add(new Track(name, seconds.getAsDouble()));
            }
        }
        if (!tracks.isEmpty()) {
            Files.writeString(pack.resolve("assets").resolve(Track.NAMESPACE).resolve("sounds.json"),
                    sounds.toString());
            Files.writeString(pack.resolve("pack.mcmeta"),
                    "{\"pack\":{\"pack_format\":1,\"description\":\"AFK camera music\"}}");
        }
        return List.copyOf(tracks);
    }

    private static void deleteTree(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> all = Files.walk(root)) {
            for (Path path : all.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        }
    }
}
