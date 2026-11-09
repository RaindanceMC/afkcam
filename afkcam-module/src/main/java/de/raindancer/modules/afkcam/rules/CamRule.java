package de.raindancer.modules.afkcam.rules;

/**
 * Whether somebody's camera starts, and whether a running one keeps going.
 *
 * <p>No check for danger nearby: the player stays exactly as they were, in their own game mode, so the camera
 * protects them from nothing — being hurt ends it, and they are hit as they would have been anyway.
 */
public final class CamRule {

    public enum Verdict {
        START, OFF, UNSUPPORTED, NOT_PERMITTED, SPECTATING, HURT, OPTED_OUT, NOT_AWAY, HELD, TOO_SOON
    }

    /**
     * @param enabled          the camera is switched on for the server
     * @param supported        this server can move a player's camera at all
     * @param permitted        the player may have it
     * @param optedIn          the player has not switched it off for themselves
     * @param away             the player counts as away right now
     * @param awaySeconds      for how long
     * @param sinceHurtSeconds since they were last hurt
     * @param spectating       in spectator mode, which has a camera of its own
     * @param held             ended the last camera themselves and has not been back since
     * @param preview          asked for it with /afkcam preview
     */
    public record Moment(boolean enabled, boolean supported, boolean permitted, boolean optedIn, boolean away,
                         double awaySeconds, double sinceHurtSeconds, boolean spectating, boolean held,
                         boolean preview) {

        public Moment withAwaySeconds(double seconds) {
            return new Moment(enabled, supported, permitted, optedIn, away, seconds, sinceHurtSeconds, spectating,
                    held, preview);
        }

        public Moment withSinceHurtSeconds(double seconds) {
            return new Moment(enabled, supported, permitted, optedIn, away, awaySeconds, seconds, spectating, held,
                    preview);
        }

        public Moment withAway(boolean now) {
            return new Moment(enabled, supported, permitted, optedIn, now, awaySeconds, sinceHurtSeconds, spectating,
                    held, preview);
        }
    }

    public Verdict decide(Moment m, double startAfterSeconds, double calmSeconds) {
        if (!m.enabled()) {
            return Verdict.OFF;
        }
        if (!m.supported()) {
            return Verdict.UNSUPPORTED;
        }
        if (!m.permitted()) {
            return Verdict.NOT_PERMITTED;
        }
        if (m.spectating()) {
            return Verdict.SPECTATING;
        }
        if (m.sinceHurtSeconds() < calmSeconds) {
            return Verdict.HURT;
        }
        if (m.preview()) {
            return Verdict.START;
        }
        if (!m.optedIn()) {
            return Verdict.OPTED_OUT;
        }
        if (!m.away()) {
            return Verdict.NOT_AWAY;
        }
        if (m.held()) {
            return Verdict.HELD;
        }
        if (m.awaySeconds() < startAfterSeconds) {
            return Verdict.TOO_SOON;
        }
        return Verdict.START;
    }

    /** For a camera already running: being hurt and pressing a key end it by event, not here. */
    public boolean keeps(Moment m) {
        if (!m.enabled() || !m.supported() || !m.permitted() || m.spectating()) {
            return false;
        }
        return m.preview() || m.away() && m.optedIn();
    }
}
