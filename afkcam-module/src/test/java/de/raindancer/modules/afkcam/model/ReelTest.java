package de.raindancer.modules.afkcam.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("the reel: still angles, cut from one to the next")
class ReelTest {

    private static final Point HEAD = new Point(100, 64, -20);
    private final Reel reel = Reel.stills(6, 2, 20);

    @Test
    @DisplayName("a cut lasts its seconds: the same cut all through, the next one after")
    void cuts() {
        assertThat(reel.cut(0)).isZero();
        assertThat(reel.cut(19.9)).isZero();
        assertThat(reel.cut(20)).isEqualTo(1);
        assertThat(reel.cut(65)).isEqualTo(3);
    }

    @Test
    @DisplayName("within a cut the camera stands still — nothing to shake")
    void stillWithinCut() {
        Point first = reel.position(HEAD, 2, 0.3);
        assertThat(reel.position(HEAD, 2, 0.3)).isEqualTo(first);
    }

    @Test
    @DisplayName("the first cut shows the player from the front, a little to the side, radius out and height up")
    void firstFromFront() {
        double facing = 0.7;
        Point at = reel.position(HEAD, 0, facing);

        double side = Math.atan2(at.z() - HEAD.z(), at.x() - HEAD.x());
        assertThat(Math.abs(Math.IEEEremainder(side - facing, 2 * Math.PI))).isBetween(0.2, 1.0);
        assertThat(Math.hypot(at.x() - HEAD.x(), at.z() - HEAD.z())).isCloseTo(6, within(1e-9));
        assertThat(at.y() - HEAD.y()).isCloseTo(2, within(1e-9));
    }

    @Test
    @DisplayName("each cut comes from a clearly different side than the one before")
    void differentSides() {
        for (int cut = 1; cut < 40; cut++) {
            Point before = reel.position(HEAD, cut - 1, 0);
            Point now = reel.position(HEAD, cut, 0);
            double a = Math.atan2(before.z() - HEAD.z(), before.x() - HEAD.x());
            double b = Math.atan2(now.z() - HEAD.z(), now.x() - HEAD.x());
            boolean fromAbove = Math.hypot(now.x() - HEAD.x(), now.z() - HEAD.z()) < 3
                    || Math.hypot(before.x() - HEAD.x(), before.z() - HEAD.z()) < 3;
            if (!fromAbove) {
                assertThat(Math.abs(Math.IEEEremainder(b - a, 2 * Math.PI))).as("cut %d", cut)
                        .isGreaterThan(Math.PI / 4);
            }
        }
    }

    @Test
    @DisplayName("the kinds take turns: close, wide from high up, low, from above")
    void kinds() {
        assertThat(reel.shots()).hasSize(4);
        assertThat(reel.position(HEAD, 1, 0).y() - HEAD.y()).isGreaterThan(5);
        assertThat(reel.position(HEAD, 2, 0).y()).isLessThan(HEAD.y());
    }

    @Test
    @DisplayName("every angle stays close: never more than twice the distance away")
    void staysClose() {
        for (int cut = 0; cut < 8; cut++) {
            assertThat(reel.position(HEAD, cut, 0).minus(HEAD).length()).as("cut %d", cut).isLessThan(12.5);
        }
    }

    @Test
    @DisplayName("no cut ever puts the camera into the head")
    void keepsDistance() {
        for (int cut = 0; cut < 40; cut++) {
            assertThat(reel.position(HEAD, cut, 1.1).minus(HEAD).length()).as("cut %d", cut).isGreaterThan(1.5);
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

    @Test
    @DisplayName("the side a player faces, in the reel's angles: facing south (yaw 0) is +z")
    void facing() {
        assertThat(Reel.facing(0)).isCloseTo(Math.PI / 2, within(1e-9));
        assertThat(Math.abs(Math.IEEEremainder(Reel.facing(90) - Math.PI, 2 * Math.PI))).isCloseTo(0, within(1e-9));
    }
}
