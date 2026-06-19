package nl.chimpgamer.mapartlock;

import nl.chimpgamer.mapartlock.command.MapArtCommand;
import nl.chimpgamer.mapartlock.config.MessageService;
import nl.chimpgamer.mapartlock.config.PluginSettings;
import nl.chimpgamer.mapartlock.listener.MapLockMenuListener;
import nl.chimpgamer.mapartlock.listener.MapProtectionListener;
import nl.chimpgamer.mapartlock.listener.PlayerMapInteractListener;
import nl.chimpgamer.mapartlock.listener.ProtectedInventoryListener;
import nl.chimpgamer.mapartlock.menu.MapLockMenu;
import nl.chimpgamer.mapartlock.permission.MapLockPermissions;
import nl.chimpgamer.mapartlock.service.MapLockService;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MapArtLockPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();

        PluginSettings settings = new PluginSettings(getConfig());
        MessageService messages = new MessageService(this);
        MapLockService mapLockService = new MapLockService(settings);
        MapLockMenu menu = new MapLockMenu(mapLockService, messages);

        PluginManager pluginManager = getServer().getPluginManager();
        registerPermissions(pluginManager);
        registerCommand("mapart", "Open de MapArtLock GUI.", List.of(), new MapArtCommand(this, settings, menu, mapLockService, messages));

        pluginManager.registerEvents(new PlayerMapInteractListener(settings, mapLockService, menu, messages), this);
        pluginManager.registerEvents(new MapLockMenuListener(this, mapLockService, menu, messages), this);
        pluginManager.registerEvents(new MapProtectionListener(settings, mapLockService), this);
        pluginManager.registerEvents(new ProtectedInventoryListener(settings, mapLockService, messages), this);

        getLogger().info("MapArtLock is ingeschakeld.");
    }

    private void registerPermissions(PluginManager pluginManager) {
        addPermission(pluginManager, new Permission(
                MapLockPermissions.USER,
                "Userrechten: /mapart openen, right-click GUI openen en eigen mapart locken/unlocken.",
                PermissionDefault.FALSE
        ));
        addPermission(pluginManager, new Permission(
                MapLockPermissions.ADMIN,
                "Adminrechten: andermans mapart locken/unlocken en /mapart version plus /mapart reload gebruiken.",
                PermissionDefault.OP,
                children(MapLockPermissions.USER)
        ));
    }

    private Map<String, Boolean> children(String... permissionNodes) {
        Map<String, Boolean> children = new LinkedHashMap<>();
        for (String permissionNode : permissionNodes) {
            children.put(permissionNode, true);
        }
        return children;
    }

    private void addPermission(PluginManager pluginManager, Permission permission) {
        if (pluginManager.getPermission(permission.getName()) == null) {
            pluginManager.addPermission(permission);
        }
    }
}
