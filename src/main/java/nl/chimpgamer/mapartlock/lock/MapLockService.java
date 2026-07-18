package nl.chimpgamer.mapartlock.lock;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.MapId;
import nl.chimpgamer.mapartlock.Permissions;
import nl.chimpgamer.mapartlock.config.Settings;
import nl.chimpgamer.mapartlock.item.MapDecorator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

/**
 * Every rule about locking lives here, so the listeners stay thin and cannot drift apart.
 */
public final class MapLockService {
    private final Settings settings;
    private final LockRegistry registry;
    private final MapDecorator decorator;

    public MapLockService(Settings settings, LockRegistry registry, MapDecorator decorator) {
        this.settings = settings;
        this.registry = registry;
        this.decorator = decorator;
    }

    public boolean isFilledMap(ItemStack itemStack) {
        return itemStack != null && itemStack.getType() == Material.FILLED_MAP;
    }

    /**
     * Read straight off the item's map_id data component: no deprecated MapMeta call, no
     * MapView lookup that returns null for an unloaded world, and no ItemMeta clone on what is
     * the hottest path in the plugin.
     */
    public OptionalInt mapId(ItemStack itemStack) {
        if (!isFilledMap(itemStack)) {
            return OptionalInt.empty();
        }

        MapId mapId = itemStack.getData(DataComponentTypes.MAP_ID);
        return mapId == null ? OptionalInt.empty() : OptionalInt.of(mapId.id());
    }

    public Optional<MapLock> lockOf(ItemStack itemStack) {
        OptionalInt mapId = mapId(itemStack);
        return mapId.isPresent() ? registry.find(mapId.getAsInt()) : Optional.empty();
    }

    public boolean isLocked(ItemStack itemStack) {
        return lockOf(itemStack).isPresent();
    }

    /** Whether this player may put the item in an anvil, crafting grid, item frame and so on. */
    public boolean mayUse(ItemStack itemStack, Player player) {
        Optional<MapLock> lock = lockOf(itemStack);
        return lock.isEmpty() || lock.get().isOwnedBy(player.getUniqueId()) || hasBypass(player);
    }

    public boolean hasBypass(Player player) {
        return settings.bypassEnabled() && Permissions.canBypass(player);
    }

    public LockOutcome lock(ItemStack itemStack, Player player) {
        OptionalInt mapId = mapId(itemStack);
        if (!isFilledMap(itemStack)) {
            return LockOutcome.NOT_A_MAP;
        }
        if (mapId.isEmpty()) {
            return LockOutcome.NO_MAP_DATA;
        }
        if (registry.isLocked(mapId.getAsInt())) {
            return LockOutcome.ALREADY_LOCKED;
        }

        MapLock lock = MapLock.now(mapId.getAsInt(), player.getUniqueId());
        registry.put(lock);
        decorator.markLocked(itemStack, ownerName(lock.owner()));
        return LockOutcome.LOCKED;
    }

    public LockOutcome unlock(ItemStack itemStack, Player player) {
        OptionalInt mapId = mapId(itemStack);
        if (!isFilledMap(itemStack)) {
            return LockOutcome.NOT_A_MAP;
        }
        if (mapId.isEmpty()) {
            return LockOutcome.NO_MAP_DATA;
        }

        Optional<MapLock> existing = registry.find(mapId.getAsInt());
        if (existing.isEmpty()) {
            return LockOutcome.ALREADY_UNLOCKED;
        }
        if (!existing.get().isOwnedBy(player.getUniqueId()) && !Permissions.canManageOthers(player)) {
            return LockOutcome.NOT_OWNER;
        }

        registry.remove(mapId.getAsInt());
        decorator.clear(itemStack);
        return LockOutcome.UNLOCKED;
    }

    /**
     * Brings an item's cosmetics in line with the registry. A copy made before the lock, or one
     * conjured with {@code /give}, carries no lore even though it is protected.
     *
     * @return true when the item changed
     */
    public boolean refreshDecoration(ItemStack itemStack) {
        if (!isFilledMap(itemStack)) {
            return false;
        }

        Optional<MapLock> lock = lockOf(itemStack);
        if (lock.isPresent()) {
            if (decorator.isDecorated(itemStack)) {
                return false;
            }
            decorator.markLocked(itemStack, ownerName(lock.get().owner()));
            return true;
        }

        if (!decorator.isDecorated(itemStack)) {
            return false;
        }
        decorator.clear(itemStack);
        return true;
    }

    public String ownerName(UUID owner) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner);
        String name = offlinePlayer.getName();
        return name == null ? owner.toString() : name;
    }

    public int lockCount() {
        return registry.size();
    }
}
