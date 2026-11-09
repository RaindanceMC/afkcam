package de.raindancer.modules.afkcam.model;

import java.util.List;

/**
 * Still angles, each held for {@code shotSeconds} and then cut to the next: the kinds take turns, and every cut
 * comes from another side — the golden angle round from the last, so sides never repeat in a pattern. The first cut
 * shows the player from the front, a little to the side.
 */
public record Reel(List<Shot> shots, double shotSeconds) {

    private static final double GOLDEN_ANGLE = Math.PI * (3 - Math.sqrt(5));
    private static final double FRONT_THREE_QUARTER = 0.5;

    public Reel {
        shots = List.copyOf(shots);
        if (shots.isEmpty()) {
            throw new IllegalArgumentException("a reel needs a shot");
        }
        if (shotSeconds <= 0) {
            throw new IllegalArgumentException("a shot needs some seconds");
        }
    }

    /** Close, wide from high up, low looking up, and from nearly straight above. */
    public static Reel stills(double radius, double height, double shotSeconds) {
        return new Reel(List.of(
                new Shot(radius, height, 0),
                new Shot(radius * 1.6, height + radius * 0.8, 0),
                new Shot(radius * 0.75, -0.6, 0),
                new Shot(1.5, height + radius * 1.2, 0)),
                shotSeconds);
    }

    /** The side a player with Minecraft yaw {@code yaw} faces, in this reel's angles (x = cos, z = sin). */
    public static double facing(float yaw) {
        double radians = Math.toRadians(yaw);
        return Math.atan2(Math.cos(radians), -Math.sin(radians));
    }

    public int cut(double seconds) {
        return (int) Math.floor(Math.max(0, seconds) / shotSeconds);
    }

    /** Where the camera stands for cut number {@code cut}, for a player who faced {@code facing} at the start. */
    public Point position(Point head, int cut, double facing) {
        Shot shot = shots.get(cut % shots.size());
        return shot.position(head, facing + FRONT_THREE_QUARTER + cut * GOLDEN_ANGLE);
    }
}
