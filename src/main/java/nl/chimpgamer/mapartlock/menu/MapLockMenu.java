package nl.chimpgamer.mapartlock.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import nl.chimpgamer.mapartlock.config.MessageService;
import nl.chimpgamer.mapartlock.service.MapLockService;
import nl.chimpgamer.mapartlock.service.MapLockService.LockInfo;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class MapLockMenu {
    public static final int SIZE = 27;
    public static final int LOCK_SLOT = 11;
    public static final int MAP_SLOT = 13;
    public static final int UNLOCK_SLOT = 15;
    public static final int INFO_SLOT = 22;

    private final MapLockService mapLockService;
    private final MessageService messages;

    public MapLockMenu(MapLockService mapLockService, MessageService messages) {
        this.mapLockService = mapLockService;
        this.messages = messages;
    }

    public void open(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (!mapLockService.isFilledMap(itemInHand)) {
            messages.send(player, "must_hold_map");
            return;
        }

        Holder holder = new Holder();
        Inventory inventory = Bukkit.createInventory(holder, SIZE, Component.text("Map Lock"));
        holder.setInventory(inventory);
        populate(inventory, itemInHand);
        player.openInventory(inventory);
    }

    public void populate(Inventory inventory, ItemStack currentMap) {
        ItemStack filler = namedItem(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));
        for (int slot = 0; slot < SIZE; slot++) {
            inventory.setItem(slot, filler);
        }

        inventory.setItem(LOCK_SLOT, namedItem(
                Material.LIME_DYE,
                Component.text("Lock", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false),
                Component.text("Bescherm deze kaart.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
        ));
        inventory.setItem(MAP_SLOT, currentMap.clone());
        inventory.setItem(UNLOCK_SLOT, namedItem(
                Material.RED_DYE,
                Component.text("Unlock", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false),
                Component.text("Verwijder de bescherming.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
        ));
        LockInfo info = mapLockService.lockInfo(currentMap);
        inventory.setItem(INFO_SLOT, info.locked() ? lockedInfoItem(info) : unlockedInfoItem());
    }

    private ItemStack namedItem(Material material, Component name, Component... lore) {
        ItemStack itemStack = new ItemStack(material);
        itemStack.editMeta(meta -> {
            meta.displayName(name);
            if (lore.length > 0) {
                meta.lore(List.of(lore));
            }
        });
        return itemStack;
    }

    private ItemStack lockedInfoItem(LockInfo info) {
        return namedItem(
                Material.BOOK,
                Component.text("Info", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false),
                Component.text("Status: Gelockt", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false),
                Component.text("Map ID: " + info.mapId(), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("Owner: " + info.ownerName(), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("Locked At: " + mapLockService.formatLockedAt(info.lockedAt()), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
        );
    }

    private ItemStack unlockedInfoItem() {
        return namedItem(
                Material.BOOK,
                Component.text("Info", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false),
                Component.text("Status: Niet gelockt", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false),
                Component.text("Deze kaart heeft nog geen lock-data.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
        );
    }

    public static final class Holder implements InventoryHolder {
        private Inventory inventory;

        private void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        @Override
        public @NotNull Inventory getInventory() {
            return inventory;
        }
    }
}
