package de.raindancer.modules.afkcam.music;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("which music a camera plays")
class MusicChoiceTest {

    @Test
    @DisplayName("custom, with tracks, to somebody wearing the pack: the owner's tracks")
    void custom() {
        assertThat(MusicChoice.of("custom", true, true)).isEqualTo(MusicChoice.PLAYLIST);
    }

    @Test
    @DisplayName("custom, but no tracks or no pack on that player: the game's menu music instead of silence")
    void fallback() {
        assertThat(MusicChoice.of("custom", false, true).key()).isEqualTo(MusicChoice.FALLBACK);
        assertThat(MusicChoice.of("custom", true, false).key()).isEqualTo(MusicChoice.FALLBACK);
    }

    @Test
    @DisplayName("a sound key plays that sound; empty plays nothing")
    void keyOrNothing() {
        assertThat(MusicChoice.of("minecraft:music.end", true, true).key()).isEqualTo("minecraft:music.end");
        assertThat(MusicChoice.of("  ", true, true)).isEqualTo(MusicChoice.NONE);
        assertThat(MusicChoice.of(null, true, true)).isEqualTo(MusicChoice.NONE);
        assertThat(MusicChoice.of("not a key!", true, true)).isEqualTo(MusicChoice.NONE);
    }
}
