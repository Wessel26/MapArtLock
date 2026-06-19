package nl.chimpgamer.mapartlock.listener;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import nl.chimpgamer.mapartlock.config.MessageService;
import nl.chimpgamer.mapartlock.menu.MapLockMenu;
import nl.chimpgamer.mapartlock.service.MapLockService;
import nl.chimpgamer.mapartlock.permission.MapLockPermissions;
import nl.chimpgamer.mapartlock.service.MapLockService.LockInfo;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class MapLockMenuListener implements Listener {
    private final JavaPlugin plugin;
    private final MapLockService mapLockService;
    private final MapLockMenu menu;
    private final MessageService messages;

    public MapLockMenuListener(JavaPlugin plugin, MapLockService mapLockService, MapLockMenu menu, MessageService messages) {
        this.plugin = plugin;
        this.mapLockService = mapLockService;
        this.menu = menu;
        this.messages = messages;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof MapLockMenu.Holder)) {
            return;
        }

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= MapLockMenu.SIZE) {
            return;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (!mapLockService.isFilledMap(itemInHand)) {
            messages.send(player, "must_hold_map");
            return;
        }

        if (slot == MapLockMenu.LOCK_SLOT) {
            lock(player, event.getInventory(), itemInHand);
        } else if (slot == MapLockMenu.UNLOCK_SLOT) {
            unlock(player, event.getInventory(), itemInHand);
        } else if (slot == MapLockMenu.INFO_SLOT) {
            sendInfo(player, itemInHand);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof MapLockMenu.Holder) {
            event.setCancelled(true);
        }
    }

    private void lock(Player player, Inventory inventory, ItemStack itemInHand) {
        if (mapLockService.isLocked(itemInHand)) {
            messages.send(player, "already_locked");
            return;
        }

        if (!mapLockService.canLock(itemInHand, player)) {
            messages.send(player, "not_owner");
            return;
        }

        mapLockService.lock(itemInHand, player);
        LockInfo info = mapLockService.lockInfo(itemInHand);
        plugin.getLogger().info(player.getName() + " locked map art " + info.mapId());
        messages.send(player, "map_locked");
        menu.populate(inventory, itemInHand);
    }

    private void unlock(Player player, Inventory inventory, ItemStack itemInHand) {
        if (!mapLockService.isLocked(itemInHand)) {
            messages.send(player, "already_unlocked");
            return;
        }

        if (!mapLockService.canUnlock(itemInHand, player)) {
            messages.send(player, "not_owner");
            return;
        }

        LockInfo info = mapLockService.lockInfo(itemInHand);
        mapLockService.unlock(itemInHand);
        plugin.getLogger().info(player.getName() + " unlocked map art " + info.mapId() + " owned by " + info.ownerName());
        messages.send(player, "map_unlocked");
        menu.populate(inventory, itemInHand);
    }

    private void sendInfo(Player player, ItemStack itemInHand) {
        LockInfo info = mapLockService.lockInfo(itemInHand);
        if (!info.locked()) {
            messages.send(player, "map_info_unlocked");
            return;
        }

        messages.send(
                player,
                "map_info",
                Placeholder.unparsed("map_id", info.mapId()),
                Placeholder.unparsed("owner", info.ownerName()),
                Placeholder.unparsed("locked", info.locked() ? "Ja" : "Nee"),
                Placeholder.unparsed("locked_at", mapLockService.formatLockedAt(info.lockedAt()))
        );
    }
}
