package de.raindancer.modules.afkcam.rules;

import de.raindancer.modules.afkcam.rules.CamRule.Moment;
import de.raindancer.modules.afkcam.rules.CamRule.Verdict;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("when the camera starts and when it keeps running")
class CamRuleTest {

    private final CamRule rule = new CamRule();

    /** Everything in favour: away for a minute, unhurt, opted in. */
    private static Moment ready() {
        return new Moment(true, true, true, true, true, 60, Double.POSITIVE_INFINITY, false, false, false);
    }

    @Test
    @DisplayName("away long enough and unhurt: it starts")
    void starts() {
        assertThat(rule.decide(ready(), 10, 20)).isEqualTo(Verdict.START);
    }

    @Test
    @DisplayName("not yet away long enough: it waits")
    void tooSoon() {
        Moment m = ready().withAwaySeconds(3);
        assertThat(rule.decide(m, 10, 20)).isEqualTo(Verdict.TOO_SOON);
    }

    @Test
    @DisplayName("hurt a moment ago: no camera — a mob farm that nicks you must not flicker it on and off")
    void hurt() {
        Moment m = ready().withSinceHurtSeconds(5);
        assertThat(rule.decide(m, 10, 20)).isEqualTo(Verdict.HURT);
    }

    @Test
    @DisplayName("each reason to refuse is its own verdict")
    void refusals() {
        Moment r = ready();
        assertThat(rule.decide(new Moment(false, true, true, true, true, 60, 1e9, false, false, false), 10, 20))
                .isEqualTo(Verdict.OFF);
        assertThat(rule.decide(new Moment(true, false, true, true, true, 60, 1e9, false, false, false), 10, 20))
                .isEqualTo(Verdict.UNSUPPORTED);
        assertThat(rule.decide(new Moment(true, true, false, true, true, 60, 1e9, false, false, false), 10, 20))
                .isEqualTo(Verdict.NOT_PERMITTED);
        assertThat(rule.decide(new Moment(true, true, true, false, true, 60, 1e9, false, false, false), 10, 20))
                .isEqualTo(Verdict.OPTED_OUT);
        assertThat(rule.decide(new Moment(true, true, true, true, false, 0, 1e9, false, false, false), 10, 20))
                .isEqualTo(Verdict.NOT_AWAY);
        assertThat(rule.decide(new Moment(true, true, true, true, true, 60, 1e9, true, false, false), 10, 20))
                .isEqualTo(Verdict.SPECTATING);
        assertThat(rule.decide(new Moment(true, true, true, true, true, 60, 1e9, false, true, false), 10, 20))
                .isEqualTo(Verdict.HELD);
        assertThat(r.away()).isTrue();
    }

    @Test
    @DisplayName("a preview needs nobody to be away or opted in, but is never shown to somebody just hurt")
    void preview() {
        Moment idle = new Moment(true, true, true, false, false, 0, 1e9, false, true, true);
        assertThat(rule.decide(idle, 10, 20)).isEqualTo(Verdict.START);
        assertThat(rule.decide(idle.withSinceHurtSeconds(1), 10, 20)).isEqualTo(Verdict.HURT);
    }

    @Test
    @DisplayName("a running camera keeps going while they stay away, and stops the moment they are back")
    void keeps() {
        assertThat(rule.keeps(ready())).isTrue();
        assertThat(rule.keeps(ready().withAway(false))).isFalse();
        assertThat(rule.keeps(new Moment(true, true, true, true, true, 60, 1e9, true, false, false))).isFalse();
        assertThat(rule.keeps(new Moment(false, true, true, true, true, 60, 1e9, false, false, false))).isFalse();
    }

    @Test
    @DisplayName("a preview keeps going even once they are not away — only a key ends it")
    void previewKeeps() {
        assertThat(rule.keeps(new Moment(true, true, true, false, false, 0, 1e9, false, false, true))).isTrue();
    }
}
