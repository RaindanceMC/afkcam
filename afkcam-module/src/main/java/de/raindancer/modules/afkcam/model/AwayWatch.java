package de.raindancer.modules.afkcam.model;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Since when each player has been away, when they were last hurt, and who ended their camera themselves.
 *
 * <p>Core's {@code Away} only says yes or no, so the start is noticed here. Somebody who ends the camera with a
 * click or the hotbar may still count as away to whoever decides that; they are held until they have been back
 * once, or the camera would come straight back.
 */
public final class AwayWatch {

    private final Map<UUID, Long> awaySince = new ConcurrentHashMap<>();
    private final Map<UUID, Long> hurtAt = new ConcurrentHashMap<>();
    private final Set<UUID> held = ConcurrentHashMap.newKeySet();

    public void observe(UUID who, boolean away, long now) {
        if (away) {
            awaySince.putIfAbsent(who, now);
        } else {
            awaySince.remove(who);
            held.remove(who);
        }
    }

    public double awaySeconds(UUID who, long now) {
        Long since = awaySince.get(who);
        return since == null ? 0 : (now - since) / 1000.0;
    }

    public void hold(UUID who) {
        held.add(who);
    }

    public boolean held(UUID who) {
        return held.contains(who);
    }

    public void hurt(UUID who, long now) {
        hurtAt.put(who, now);
    }

    public double sinceHurtSeconds(UUID who, long now) {
        Long at = hurtAt.get(who);
        return at == null ? Double.POSITIVE_INFINITY : (now - at) / 1000.0;
    }

    public void forget(UUID who) {
        awaySince.remove(who);
        hurtAt.remove(who);
        held.remove(who);
    }
}
