package de.raindancer.modules.afkcam.rules;

/**
 * Whether a camera may hide the player's arm and HUD by telling their client it is a spectator. The server keeps
 * the real game mode, so farms and pickup run on — but a client that believes it is a spectator has no physics:
 * it does not fall, water does not push it, a minecart does not carry it. So only for somebody standing on solid
 * ground with nothing moving them, looked at again while the camera runs.
 */
public final class HudRule {

    private HudRule() {
    }

    public record Footing(boolean wanted, boolean supported, boolean bedrock, boolean onSolidGround, boolean riding,
                          boolean inLiquid, boolean gliding, boolean flying) {
    }

    public static boolean hides(Footing f) {
        return f.wanted() && f.supported() && !f.bedrock() && f.onSolidGround()
                && !f.riding() && !f.inLiquid() && !f.gliding() && !f.flying();
    }
}
