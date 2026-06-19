package nl.chimpgamer.mapartlock.listener;

import nl.chimpgamer.mapartlock.config.PluginSettings;
import nl.chimpgamer.mapartlock.service.MapLockService;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareInventoryResultEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class MapProtectionListener implements Listener {
    private final PluginSettings settings;
    private final MapLockService mapLockService;

    public MapProtectionListener(PluginSettings settings, MapLockService mapLockService) {
        this.settings = settings;
        this.mapLockService = mapLockService;
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        if (!settings.blockCrafting()) {
            return;
        }

        if (containsBlockedLockedMap(event, event.getInventory().getMatrix())) {
            event.getInventory().setResult(null);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCrafterCraft(CrafterCraftEvent event) {
        // CrafterCraftEvent has no player context. Manual insertion is protected by
        // ProtectedInventoryListener, and hopper insertion is blocked separately.
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (!settings.blockAnvil()) {
            return;
        }

        Inventory inventory = event.getInventory();
        if (containsBlockedLockedMap(event, inventory.getItem(0), inventory.getItem(1))) {
            event.setResult(null);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPrepareInventoryResult(PrepareInventoryResultEvent event) {
        if (!settings.blockCartography() || event.getInventory().getType() != InventoryType.CARTOGRAPHY) {
            return;
        }

        if (containsBlockedLockedMap(event, event.getInventory().getContents())) {
            event.setResult(null);
        }
    }

    private boolean containsBlockedLockedMap(InventoryEvent event, ItemStack... contents) {
        if (contents == null) {
            return false;
        }

        for (ItemStack itemStack : contents) {
            if (isBlockedForViewers(event, itemStack)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBlockedForViewers(InventoryEvent event, ItemStack itemStack) {
        if (!mapLockService.isLocked(itemStack)) {
            return false;
        }

        for (HumanEntity viewer : event.getInventory().getViewers()) {
            if (viewer instanceof Player player && canUse(player, itemStack)) {
                return false;
            }
        }
        return true;
    }

    private boolean canUse(Player player, ItemStack itemStack) {
        return mapLockService.canUseInProtectedInventory(itemStack, player);
    }
}
