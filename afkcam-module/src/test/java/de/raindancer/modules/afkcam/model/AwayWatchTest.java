package de.raindancer.modules.afkcam.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("the away watch: since when somebody is away, and whether they just came back")
class AwayWatchTest {

    private final UUID who = UUID.randomUUID();
    private final AwayWatch watch = new AwayWatch();

    @Test
    @DisplayName("away time counts from the first moment they were seen away")
    void counts() {
        watch.observe(who, true, 1_000);
        watch.observe(who, true, 5_000);

        assertThat(watch.awaySeconds(who, 11_000)).isEqualTo(10.0);
    }

    @Test
    @DisplayName("back, the count starts over")
    void startsOver() {
        watch.observe(who, true, 1_000);
        watch.observe(who, false, 2_000);
        watch.observe(who, true, 9_000);

        assertThat(watch.awaySeconds(who, 10_000)).isEqualTo(1.0);
        assertThat(watch.awaySeconds(UUID.randomUUID(), 10_000)).isZero();
    }

    @Test
    @DisplayName("somebody who pressed a key is held until they were back once — a click alone does not count as back")
    void held() {
        watch.observe(who, true, 1_000);
        watch.hold(who);
        watch.observe(who, true, 2_000);

        assertThat(watch.held(who)).isTrue();

        watch.observe(who, false, 3_000);
        assertThat(watch.held(who)).isFalse();
    }

    @Test
    @DisplayName("hurt time is remembered")
    void hurt() {
        watch.hurt(who, 4_000);

        assertThat(watch.sinceHurtSeconds(who, 6_000)).isEqualTo(2.0);
        assertThat(watch.sinceHurtSeconds(UUID.randomUUID(), 6_000)).isEqualTo(Double.POSITIVE_INFINITY);
    }
}
