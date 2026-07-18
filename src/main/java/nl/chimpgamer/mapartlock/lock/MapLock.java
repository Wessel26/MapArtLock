package nl.chimpgamer.mapartlock.lock;

import java.time.Instant;
import java.util.UUID;

/**
 * A lock on one piece of map art, keyed by the vanilla map id.
 *
 * <p>That id is what makes the protection hold: every copy of a filled map carries the same id
 * and points at the same {@code world/data/map_<id>.dat}, so one lock covers all of them —
 * including a copy conjured with {@code /give}.
 */
public record MapLock(int mapId, UUID owner, Instant lockedAt) {
    public MapLock {
        if (mapId < 0) {
            throw new IllegalArgumentException("mapId moet 0 of hoger zijn, was " + mapId);
        }
        if (owner == null) {
            throw new IllegalArgumentException("owner mag niet null zijn");
        }
        if (lockedAt == null) {
            throw new IllegalArgumentException("lockedAt mag niet null zijn");
        }
    }

    public static MapLock now(int mapId, UUID owner) {
        return new MapLock(mapId, owner, Instant.now());
    }

    public boolean isOwnedBy(UUID candidate) {
        return owner.equals(candidate);
    }
}
