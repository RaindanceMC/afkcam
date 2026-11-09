package de.raindancer.modules.afkcam.model;

/**
 * One way of circling the player: how far out, how high, how fast and from which side.
 *
 * <p>The angle comes from the reel's clock, not the shot's own, so two shots can be blended at any moment without
 * the camera swinging round to catch up.
 *
 * @param radius blocks out from the head
 * @param height blocks above the head, below it when negative
 * @param speed  turns per {@code turnSeconds}; negative goes the other way round
 * @param phase  where on the circle it is at time zero, in radians
 */
public record Shot(double radius, double height, double speed, double phase) {

    public Point position(Point head, double seconds, double turnSeconds) {
        double angle = phase + speed * 2 * Math.PI * seconds / turnSeconds;
        return head.plus(new Point(radius * Math.cos(angle), height, radius * Math.sin(angle)));
    }
}
