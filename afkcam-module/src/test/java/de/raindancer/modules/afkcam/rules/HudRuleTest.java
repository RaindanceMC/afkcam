package de.raindancer.modules.afkcam.rules;

import de.raindancer.modules.afkcam.rules.HudRule.Footing;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("when the client may believe it is a spectator, so arm and HUD go")
class HudRuleTest {

    /** Standing still on solid ground, Java client, setting on. */
    private static Footing standing() {
        return new Footing(true, true, false, true, false, false, false, false);
    }

    @Test
    @DisplayName("standing on solid ground: the HUD goes")
    void standingHides() {
        assertThat(HudRule.hides(standing())).isTrue();
    }

    @Test
    @DisplayName("the owner turned it off, or the server cannot send it: the HUD stays")
    void offOrUnsupported() {
        assertThat(HudRule.hides(new Footing(false, true, false, true, false, false, false, false))).isFalse();
        assertThat(HudRule.hides(new Footing(true, false, false, true, false, false, false, false))).isFalse();
    }

    @Test
    @DisplayName("a Bedrock player: never — Geyser would hand them a spectator's free flight")
    void bedrock() {
        assertThat(HudRule.hides(new Footing(true, true, true, true, false, false, false, false))).isFalse();
    }

    @Test
    @DisplayName("nothing solid underfoot: never — a spectator's client does not fall, the server would see "
            + "them float")
    void noGround() {
        assertThat(HudRule.hides(new Footing(true, true, false, false, false, false, false, false))).isFalse();
    }

    @Test
    @DisplayName("riding, in water or lava, gliding or flying: never — the client would stop moving with it")
    void moving() {
        assertThat(HudRule.hides(new Footing(true, true, false, true, true, false, false, false))).isFalse();
        assertThat(HudRule.hides(new Footing(true, true, false, true, false, true, false, false))).isFalse();
        assertThat(HudRule.hides(new Footing(true, true, false, true, false, false, true, false))).isFalse();
        assertThat(HudRule.hides(new Footing(true, true, false, true, false, false, false, true))).isFalse();
    }
}
