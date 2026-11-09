package de.raindancer.modules.afkcam.model;

/** A spot in a world, free of Bukkit so the camera's path can be worked out in a test. */
public record Point(double x, double y, double z) {

    public Point plus(Point other) {
        return new Point(x + other.x, y + other.y, z + other.z);
    }

    public Point minus(Point other) {
        return new Point(x - other.x, y - other.y, z - other.z);
    }

    public Point times(double factor) {
        return new Point(x * factor, y * factor, z * factor);
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    /** {@code this} towards {@code other} by {@code share}, 0 to 1. */
    public Point towards(Point other, double share) {
        return plus(other.minus(this).times(share));
    }
}
