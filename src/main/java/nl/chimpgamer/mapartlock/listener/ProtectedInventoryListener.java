package nl.chimpgamer.mapartlock.listener;

import nl.chimpgamer.mapartlock.config.MessageService;
import nl.chimpgamer.mapartlock.config.PluginSettings;
import nl.chimpgamer.mapartlock.permission.MapLockPermissions;
import nl.chimpgamer.mapartlock.service.MapLockService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class ProtectedInventoryListener implements Listener {
    private final PluginSettings settings;
    private final MapLockService mapLockService;
    private final MessageService messages;

    public ProtectedInventoryListener(PluginSettings settings, MapLockService mapLockService, MessageService messages) {
        this.settings = settings;
        this.mapLockService = mapLockService;
        this.messages = messages;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        if (!isProtectedInventory(topInventory)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (canBypass(player)) {
            return;
        }

        int rawSlot = event.getRawSlot();
        boolean clickedTopInventory = rawSlot >= 0 && rawSlot < topInventory.getSize();
        boolean blocked = false;

        if (clickedTopInventory && cannotUse(player, event.getCurrentItem())) {
            blocked = true;
        }

        if (event.getAction().name().equals("MOVE_TO_OTHER_INVENTORY") && cannotUse(player, event.getCurrentItem())) {
            blocked = true;
        }

        if (clickedTopInventory && cannotUse(player, event.getCursor())) {
            blocked = true;
        }

        if (clickedTopInventory && event.getClick() == ClickType.NUMBER_KEY && cannotUseHotbarItem(player, event.getHotbarButton())) {
            blocked = true;
        }

        if (clickedTopInventory && event.getClick() == ClickType.SWAP_OFFHAND && cannotUse(player, player.getInventory().getItemInOffHand())) {
            blocked = true;
        }

        if (blocked) {
            event.setCancelled(true);
            messages.send(player, "protected_inventory_blocked");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        if (!isProtectedInventory(topInventory)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player) || canBypass(player)) {
            return;
        }

        boolean dragsLockedMapIntoTopInventory = event.getNewItems().entrySet().stream()
                .anyMatch(entry -> entry.getKey() < topInventory.getSize() && cannotUse(player, entry.getValue()));
        if (dragsLockedMapIntoTopInventory) {
            event.setCancelled(true);
            messages.send(player, "protected_inventory_blocked");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (isProtectedInventory(event.getDestination()) && mapLockService.isLocked(event.getItem())) {
            event.setCancelled(true);
        }
    }

    private boolean cannotUseHotbarItem(Player player, int hotbarSlot) {
        if (hotbarSlot < 0) {
            return false;
        }
        ItemStack itemStack = player.getInventory().getItem(hotbarSlot);
        return cannotUse(player, itemStack);
    }

    private boolean cannotUse(Player player, ItemStack itemStack) {
        return !mapLockService.canUseInProtectedInventory(itemStack, player);
    }

    private boolean canBypass(Player player) {
        return settings.adminBypassProtection() && MapLockPermissions.canBypassProtection(player);
    }

    private boolean isProtectedInventory(Inventory inventory) {
        InventoryType type = inventory.getType();
        return switch (type.name()) {
            case "ANVIL" -> settings.blockAnvil();
            case "CARTOGRAPHY" -> settings.blockCartography();
            case "CRAFTER" -> settings.blockCrafter();
            case "WORKBENCH" -> settings.blockCrafting();
            default -> false;
        };
    }
}
