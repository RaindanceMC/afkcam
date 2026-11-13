package de.raindancer.modules.afkcam.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Which tip shows above the hotbar while somebody is away: none for the first seconds, which say how to come back,
 * then a new one every {@link #PERIOD_MILLIS}, in an order shuffled once per time away, every tip before any repeats.
 */
public final class TipRule {

    public static final long FIRST_TIP_MILLIS = 8_000;
    public static final long PERIOD_MILLIS = 12_000;

    private TipRule() {
    }

    /**
     * @param away  how long they have been away
     * @param seed  one per time away (when it began), so the order differs from the last time
     * @param count how many tips there are
     * @return the tip's index, or -1 for none
     */
    public static int tipAt(long away, long seed, int count) {
        if (count <= 0 || away < FIRST_TIP_MILLIS) {
            return -1;
        }
        long turn = (away - FIRST_TIP_MILLIS) / PERIOD_MILLIS;
        long round = turn / count;
        List<Integer> order = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            order.add(i);
        }
        Collections.shuffle(order, new Random(seed * 31 + round));
        return order.get((int) (turn % count));
    }
}
