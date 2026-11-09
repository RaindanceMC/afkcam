package de.raindancer.modules.afkcam.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("the reel: where the camera is at any moment")
class ReelTest {

    private static final Point HEAD = new Point(100, 64, -20);

    @Test
    @DisplayName("the first shot starts radius away and height above the head")
    void firstShot() {
        Reel reel = Reel.standard(6, 2, 80, 20, 4);

        Point at = reel.position(HEAD, 0);

        assertThat(Math.hypot(at.x() - HEAD.x(), at.z() - HEAD.z())).isCloseTo(6, within(1e-9));
        assertThat(at.y() - HEAD.y()).isCloseTo(2, within(1e-9));
    }

    @Test
    @DisplayName("an orbit goes a quarter way round in a quarter of a turn")
    void orbits() {
        Shot orbit = new Shot(6, 2, 1, 0);

        Point start = orbit.position(HEAD, 0, 80);
        Point quarter = orbit.position(HEAD, 20, 80);

        double a = Math.atan2(start.z() - HEAD.z(), start.x() - HEAD.x());
        double b = Math.atan2(quarter.z() - HEAD.z(), quarter.x() - HEAD.x());
        assertThat(Math.abs(Math.IEEEremainder(b - a, 2 * Math.PI))).isCloseTo(Math.PI / 2, within(1e-9));
    }

    @Test
    @DisplayName("from shot to shot the camera glides — it never jumps, over several whole cycles")
    void neverJumps() {
        Reel reel = Reel.standard(6, 2, 80, 20, 4);
        double step = 0.05;
        Point last = reel.position(HEAD, 0);
        for (double t = step; t < reel.cycleSeconds() * 3; t += step) {
            Point now = reel.position(HEAD, t);
            assertThat(now.minus(last).length()).as("at %.2f s", t).isLessThan(0.6);
            last = now;
        }
    }

    @Test
    @DisplayName("the reel changes shot: halfway through the second shot the camera is somewhere else than an orbit")
    void changesShot() {
        Reel reel = Reel.standard(6, 2, 80, 20, 4);

        Point second = reel.position(HEAD, 30);

        assertThat(second.y() - HEAD.y()).isNotCloseTo(2, within(0.5));
        assertThat(reel.cycleSeconds()).isEqualTo(20.0 * reel.shots().size());
        assertThat(reel.shots()).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("no shot ever puts the camera into the head")
    void keepsDistance() {
        Reel reel = Reel.standard(6, 2, 80, 20, 4);
        for (double t = 0; t < reel.cycleSeconds(); t += 0.25) {
            assertThat(reel.position(HEAD, t).minus(HEAD).length()).as("at %.2f s", t).isGreaterThan(1.5);
        }
    }

    @Test
    @DisplayName("the camera looks at the head: from the east it faces west, from above it looks down")
    void looks() {
        Pose east = Pose.looking(new Point(10, 0, 0), new Point(0, 0, 0));
        Pose above = Pose.looking(new Point(0, 5, 5), new Point(0, 0, 0));
        Pose south = Pose.looking(new Point(0, 0, 10), new Point(0, 0, 0));

        assertThat(east.yaw()).isCloseTo(90f, within(1e-4f));
        assertThat(east.pitch()).isCloseTo(0f, within(1e-4f));
        assertThat(above.pitch()).isCloseTo(45f, within(1e-4f));
        assertThat(Math.abs(south.yaw())).isCloseTo(180f, within(1e-4f));
    }
}
