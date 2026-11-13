package de.raindancer.modules.afkcam.rules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** Tips above the hotbar while away: not at once, a new one now and then, every tip before any comes again. */
class TipRuleTest {

    @Test
    @DisplayName("the first seconds say how to come back; then a tip, changing every period")
    void whenATip() {
        assertThat(TipRule.tipAt(0, 7, 10)).isEqualTo(-1);
        assertThat(TipRule.tipAt(TipRule.FIRST_TIP_MILLIS - 1, 7, 10)).isEqualTo(-1);
        int first = TipRule.tipAt(TipRule.FIRST_TIP_MILLIS, 7, 10);
        assertThat(first).isBetween(0, 9);
        assertThat(TipRule.tipAt(TipRule.FIRST_TIP_MILLIS + TipRule.PERIOD_MILLIS - 1, 7, 10)).isEqualTo(first);
    }

    @Test
    @DisplayName("every tip comes once before any comes twice, and the order differs from one time away to the next")
    void everyTipOnce() {
        List<Integer> seen = new ArrayList<>();
        for (int n = 0; n < 10; n++) {
            seen.add(TipRule.tipAt(TipRule.FIRST_TIP_MILLIS + n * TipRule.PERIOD_MILLIS, 42, 10));
        }
        assertThat(new HashSet<>(seen)).hasSize(10);
        Set<List<Integer>> orders = new HashSet<>();
        for (long seed = 1; seed <= 5; seed++) {
            List<Integer> order = new ArrayList<>();
            for (int n = 0; n < 10; n++) {
                order.add(TipRule.tipAt(TipRule.FIRST_TIP_MILLIS + n * TipRule.PERIOD_MILLIS, seed, 10));
            }
            orders.add(order);
        }
        assertThat(orders).hasSizeGreaterThan(1);
    }

    @Test
    @DisplayName("no tips written, no tips shown")
    void none() {
        assertThat(TipRule.tipAt(60_000, 7, 0)).isEqualTo(-1);
    }
}
