package de.raindancer.modules.afkcam.model;

/**
 * How far a shot is raised above where the reel wants it, when leaves or a wall are in the way: a camera squeezed
 * against the player's face is no shot, while from a little higher it usually sees over the trees.
 */
public final class Lift {

    /** How much of the shot's distance has to be free for a height to count as clear. */
    static final double CLEAR_ENOUGH = 0.75;

    private final double step;

    /** @param step blocks between two heights tried */
    public Lift(double step) {
        this.step = step;
    }

    /** Blocks above the shot for height number {@code index}. */
    public double blocks(int index) {
        return index * step;
    }

    /**
     * @param clearShares for each height from the lowest, how much of the way from the head is free, 0 to 1
     * @return the index of the height to go to
     */
    public static int choose(double[] clearShares) {
        int best = 0;
        for (int i = 0; i < clearShares.length; i++) {
            if (clearShares[i] >= CLEAR_ENOUGH) {
                return i;
            }
            if (clearShares[i] > clearShares[best]) {
                best = i;
            }
        }
        return best;
    }
}
