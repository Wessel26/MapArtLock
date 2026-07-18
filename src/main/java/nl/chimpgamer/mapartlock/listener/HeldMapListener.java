package nl.chimpgamer.mapartlock.listener;

import nl.chimpgamer.mapartlock.config.Settings;
import nl.chimpgamer.mapartlock.lock.MapLockService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

/**
 * Keeps the lore on a held map honest. Because locks are stored per map id, a copy can be
 * protected while carrying no lore at all — this is what fixes that on sight.
 */
public final class HeldMapListener implements Listener {
    private final Plugin plugin;
    private final Settings settings;
    private final MapLockService service;

    public HeldMapListener(Plugin plugin, Settings settings, MapLockService service) {
        this.plugin = plugin;
        this.settings = settings;
        this.service = service;
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemHeld(PlayerItemHeldEvent event) {
        if (!settings.syncLoreWhenHeld()) {
            return;
        }

        Player player = event.getPlayer();
        int slot = event.getNewSlot();
        if (!service.isFilledMap(player.getInventory().getItem(slot))) {
            return;
        }

        // The slot change has not landed yet, so writing the item back now can desync the
        // client. Do it once the switch is through.
        plugin.getServer().getScheduler().runTask(plugin, () -> refresh(player, slot));
    }

    private void refresh(Player player, int slot) {
        if (!player.isOnline()) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        ItemStack itemStack = inventory.getItem(slot);
        if (service.isFilledMap(itemStack) && service.refreshDecoration(itemStack)) {
            inventory.setItem(slot, itemStack);
        }
    }
}
