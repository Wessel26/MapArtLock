package nl.chimpgamer.mapartlock.lock;

import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Keeps locks in the persistent data container of the main world, so they end up in that
 * world's level.dat.
 *
 * <p>That is deliberate: the artwork itself lives in {@code world/data/map_<id>.dat}, so locks
 * and maps share one lifecycle. Restore a world backup and both roll back together, instead of
 * leaving locks pointing at maps that no longer exist.
 *
 * <p>Each lock is a nested container rather than one serialised blob, so a single unreadable
 * entry cannot take the rest with it.
 */
public final class WorldLockStorage {
    private final Supplier<World> world;
    private final Logger logger;
    private final NamespacedKey locksKey;
    private final NamespacedKey mapIdKey;
    private final NamespacedKey ownerKey;
    private final NamespacedKey lockedAtKey;

    public WorldLockStorage(Plugin plugin, Supplier<World> world) {
        this.world = world;
        this.logger = plugin.getLogger();
        this.locksKey = new NamespacedKey(plugin, "locks");
        this.mapIdKey = new NamespacedKey(plugin, "map_id");
        this.ownerKey = new NamespacedKey(plugin, "owner");
        this.lockedAtKey = new NamespacedKey(plugin, "locked_at");
    }

    public Collection<MapLock> load() {
        World target = world.get();
        if (target == null) {
            logger.severe("Geen wereld beschikbaar; er zijn geen locks geladen.");
            return List.of();
        }

        List<PersistentDataContainer> entries =
                target.getPersistentDataContainer().get(locksKey, PersistentDataType.LIST.dataContainers());
        if (entries == null) {
            return List.of();
        }

        List<MapLock> locks = new ArrayList<>(entries.size());
        int skipped = 0;
        for (PersistentDataContainer entry : entries) {
            MapLock lock = read(entry);
            if (lock == null) {
                skipped++;
            } else {
                locks.add(lock);
            }
        }

        if (skipped > 0) {
            logger.warning(skipped + " onleesbare lock(s) overgeslagen.");
        }
        return locks;
    }

    private MapLock read(PersistentDataContainer entry) {
        Integer mapId = entry.get(mapIdKey, PersistentDataType.INTEGER);
        Long lockedAt = entry.get(lockedAtKey, PersistentDataType.LONG);
        if (mapId == null || lockedAt == null) {
            return null;
        }

        try {
            UUID owner = entry.get(ownerKey, UuidType.INSTANCE);
            return owner == null ? null : new MapLock(mapId, owner, Instant.ofEpochSecond(lockedAt));
        } catch (IllegalArgumentException malformed) {
            return null;
        }
    }

    public void save(Collection<MapLock> locks) {
        World target = world.get();
        if (target == null) {
            logger.severe("Geen wereld beschikbaar; wijzigingen blijven alleen in het geheugen.");
            return;
        }

        PersistentDataContainer container = target.getPersistentDataContainer();
        if (locks.isEmpty()) {
            container.remove(locksKey);
            return;
        }

        List<PersistentDataContainer> entries = new ArrayList<>(locks.size());
        for (MapLock lock : locks) {
            PersistentDataContainer entry = container.getAdapterContext().newPersistentDataContainer();
            entry.set(mapIdKey, PersistentDataType.INTEGER, lock.mapId());
            entry.set(ownerKey, UuidType.INSTANCE, lock.owner());
            entry.set(lockedAtKey, PersistentDataType.LONG, lock.lockedAt().getEpochSecond());
            entries.add(entry);
        }

        container.set(locksKey, PersistentDataType.LIST.dataContainers(), entries);
    }
}
