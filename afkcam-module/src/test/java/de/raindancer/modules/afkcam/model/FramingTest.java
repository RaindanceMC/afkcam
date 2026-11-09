package de.raindancer.modules.afkcam.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("framing: the camera never looks through a wall")
class FramingTest {

    private static final Point HEAD = new Point(0, 64, 0);
    private static final Point WANTED = new Point(6, 64, 0);

    @Test
    @DisplayName("with nothing in the way the camera is where the shot wants it")
    void clear() {
        Framing framing = new Framing(0.3, 2);

        assertThat(framing.frame(HEAD, WANTED, Double.POSITIVE_INFINITY, 0.05)).isEqualTo(WANTED);
    }

    @Test
    @DisplayName("a wall pulls the camera in front of it at once")
    void wall() {
        Framing framing = new Framing(0.3, 2);
        framing.frame(HEAD, WANTED, Double.POSITIVE_INFINITY, 0.05);

        Point at = framing.frame(HEAD, WANTED, 3, 0.05);

        assertThat(at.x()).isCloseTo(2.7, within(1e-9));
        assertThat(at.z()).isCloseTo(0, within(1e-9));
    }

    @Test
    @DisplayName("once the wall is gone it backs out slowly instead of jumping")
    void backsOut() {
        Framing framing = new Framing(0.3, 2);
        framing.frame(HEAD, WANTED, 3, 0.05);

        Point next = framing.frame(HEAD, WANTED, Double.POSITIVE_INFINITY, 0.05);

        assertThat(next.x()).isCloseTo(2.8, within(1e-9));
        for (int i = 0; i < 100; i++) {
            next = framing.frame(HEAD, WANTED, Double.POSITIVE_INFINITY, 0.05);
        }
        assertThat(next).isEqualTo(WANTED);
    }

    @Test
    @DisplayName("boxed in, it stays just outside the head rather than inside it")
    void boxedIn() {
        Framing framing = new Framing(0.3, 2);

        Point at = framing.frame(HEAD, WANTED, 0.2, 0.05);

        assertThat(at.minus(HEAD).length()).isCloseTo(Framing.CLOSEST, within(1e-9));
    }
}
