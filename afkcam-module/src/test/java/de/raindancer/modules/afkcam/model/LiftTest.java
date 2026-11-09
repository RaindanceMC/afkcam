package de.raindancer.modules.afkcam.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("the lift: a blocked shot cranes up instead of squeezing into the player's face")
class LiftTest {

    @Test
    @DisplayName("a clear shot needs no lift")
    void clear() {
        assertThat(Lift.choose(new double[]{1, 1, 1})).isZero();
    }

    @Test
    @DisplayName("the lowest height with a clear enough view wins")
    void lowestClear() {
        assertThat(Lift.choose(new double[]{0.2, 0.5, 0.9, 1})).isEqualTo(2);
    }

    @Test
    @DisplayName("nowhere clear: the height that sees furthest")
    void bestOfBad() {
        assertThat(Lift.choose(new double[]{0.1, 0.4, 0.3})).isEqualTo(1);
    }

    @Test
    @DisplayName("the camera rises and sinks no faster than it may, and settles on the height")
    void eases() {
        Lift lift = new Lift(2, 1.5);

        assertThat(lift.ease(4, 0.05)).isCloseTo(0.075, within(1e-9));
        for (int i = 0; i < 200; i++) {
            lift.ease(4, 0.05);
        }
        assertThat(lift.ease(4, 0.05)).isEqualTo(4);
        assertThat(lift.ease(0, 0.05)).isCloseTo(4 - 0.075, within(1e-9));
        assertThat(lift.blocks(3)).isEqualTo(6);
    }
}
