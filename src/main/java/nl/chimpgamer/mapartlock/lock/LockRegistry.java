package nl.chimpgamer.mapartlock.lock;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Every lock, in memory, backed by {@link WorldLockStorage}.
 *
 * <p>Lookups never touch storage: "is this map locked?" runs on every crafting check, so it has
 * to stay a map lookup. Changes are written through immediately — the world container is an
 * in-memory structure the server persists during its own save, so there is nothing to batch and
 * no shutdown hook needed.
 */
public final class LockRegistry {
    private final WorldLockStorage storage;
    private final Map<Integer, MapLock> locks = new ConcurrentHashMap<>();

    public LockRegistry(WorldLockStorage storage) {
        this.storage = storage;
    }

    /** Replaces everything in memory with what storage holds. */
    public void load() {
        locks.clear();
        for (MapLock lock : storage.load()) {
            locks.put(lock.mapId(), lock);
        }
    }

    public Optional<MapLock> find(int mapId) {
        return Optional.ofNullable(locks.get(mapId));
    }

    public boolean isLocked(int mapId) {
        return locks.containsKey(mapId);
    }

    public void put(MapLock lock) {
        locks.put(lock.mapId(), lock);
        storage.save(List.copyOf(locks.values()));
    }

    public boolean remove(int mapId) {
        if (locks.remove(mapId) == null) {
            return false;
        }
        storage.save(List.copyOf(locks.values()));
        return true;
    }

    public int size() {
        return locks.size();
    }
}
