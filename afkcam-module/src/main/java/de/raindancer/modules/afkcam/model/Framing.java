package de.raindancer.modules.afkcam.model;

/**
 * Keeps the camera on the player's side of any wall: pulled in at once when something comes between them, backed
 * out again slowly once it is gone, the way a third-person camera does. One per running camera.
 */
public final class Framing {

    /** Never nearer than this, or the camera is inside the player's own head. */
    public static final double CLOSEST = 0.8;

    private final double margin;
    private final double growPerSecond;
    private double distance = -1;

    /**
     * @param margin        how far in front of a wall the camera stays
     * @param growPerSecond how fast it backs out once the way is clear, in blocks per second
     */
    public Framing(double margin, double growPerSecond) {
        this.margin = margin;
        this.growPerSecond = growPerSecond;
    }

    /**
     * @param clear how far from the head towards {@code wanted} is free of blocks; infinite when nothing is hit
     * @param dt    seconds since the last frame
     */
    public Point frame(Point head, Point wanted, double clear, double dt) {
        Point offset = wanted.minus(head);
        double want = offset.length();
        if (want == 0) {
            return wanted;
        }
        double allowed = Math.max(CLOSEST, Math.min(want, clear - margin));
        if (distance < 0 || allowed < distance) {
            distance = allowed;
        } else {
            distance = Math.min(allowed, distance + growPerSecond * dt);
        }
        return distance == want ? wanted : head.plus(offset.times(distance / want));
    }
}
