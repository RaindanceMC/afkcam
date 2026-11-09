package de.raindancer.modules.afkcam.model;

/**
 * One kind of still angle: how far out, how high, and how far round from where the reel puts it.
 *
 * @param radius blocks out from the head
 * @param height blocks above the head, below it when negative
 * @param turn   radians added to the cut's side
 */
public record Shot(double radius, double height, double turn) {

    public Point position(Point head, double angle) {
        double a = angle + turn;
        return head.plus(new Point(radius * Math.cos(a), height, radius * Math.sin(a)));
    }
}
