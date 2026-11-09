package de.raindancer.modules.afkcam.model;

import java.util.List;

/**
 * The shots one after another, looping, each gliding into the next over the last seconds of its time.
 */
public record Reel(List<Shot> shots, double turnSeconds, double shotSeconds, double blendSeconds) {

    public Reel {
        shots = List.copyOf(shots);
        if (shots.isEmpty()) {
            throw new IllegalArgumentException("a reel needs a shot");
        }
        blendSeconds = Math.min(blendSeconds, shotSeconds);
    }

    /**
     * Close orbit, a wide high establishing shot going the other way, a low shot looking up, then a slow one from
     * nearly straight above.
     */
    public static Reel standard(double radius, double height, double turnSeconds, double shotSeconds,
                                double blendSeconds) {
        return new Reel(List.of(
                new Shot(radius, height, 1, 0),
                new Shot(radius * 2.2, height + radius * 1.2, -0.5, Math.PI / 3),
                new Shot(radius * 0.7, -0.6, 1.3, Math.PI),
                new Shot(2, height + radius * 1.6, 0.3, Math.PI / 2)),
                turnSeconds, shotSeconds, blendSeconds);
    }

    public double cycleSeconds() {
        return shotSeconds * shots.size();
    }

    public Point position(Point head, double seconds) {
        double inCycle = seconds % cycleSeconds();
        int index = (int) (inCycle / shotSeconds) % shots.size();
        double intoShot = inCycle - index * shotSeconds;
        Point here = shots.get(index).position(head, seconds, turnSeconds);
        double blendFrom = shotSeconds - blendSeconds;
        if (blendSeconds <= 0 || intoShot < blendFrom) {
            return here;
        }
        Point next = shots.get((index + 1) % shots.size()).position(head, seconds, turnSeconds);
        double share = (intoShot - blendFrom) / blendSeconds;
        return here.towards(next, share * share * (3 - 2 * share));
    }
}
