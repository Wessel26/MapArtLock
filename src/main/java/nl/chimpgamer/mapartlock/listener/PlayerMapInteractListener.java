package nl.chimpgamer.mapartlock.listener;

import nl.chimpgamer.mapartlock.config.MessageService;
import nl.chimpgamer.mapartlock.config.PluginSettings;
import nl.chimpgamer.mapartlock.menu.MapLockMenu;
import nl.chimpgamer.mapartlock.permission.MapLockPermissions;
import nl.chimpgamer.mapartlock.service.MapLockService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class PlayerMapInteractListener implements Listener {
    private final PluginSettings settings;
    private final MapLockService mapLockService;
    private final MapLockMenu menu;
    private final MessageService messages;

    public PlayerMapInteractListener(
            PluginSettings settings,
            MapLockService mapLockService,
            MapLockMenu menu,
            MessageService messages
    ) {
        this.settings = settings;
        this.mapLockService = mapLockService;
        this.menu = menu;
        this.messages = messages;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!settings.openOnRightClick() || event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (!mapLockService.isFilledMap(event.getItem())) {
            return;
        }

        if (!MapLockPermissions.canOpenMapArt(event.getPlayer())) {
            messages.send(event.getPlayer(), "no_permission");
            return;
        }

        event.setCancelled(true);
        menu.open(event.getPlayer());
    }

}
