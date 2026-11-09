package de.raindancer.modules.afkcam.model;

/** Where the camera is and which way it faces, in the game's degrees. */
public record Pose(Point at, float yaw, float pitch) {

    /** Standing at {@code from}, facing {@code target}. Yaw 0 faces +Z, 90 faces −X; positive pitch looks down. */
    public static Pose looking(Point from, Point target) {
        Point d = target.minus(from);
        double yaw = Math.toDegrees(Math.atan2(-d.x(), d.z()));
        double pitch = Math.toDegrees(-Math.atan2(d.y(), Math.hypot(d.x(), d.z())));
        return new Pose(from, (float) yaw, (float) pitch);
    }
}
