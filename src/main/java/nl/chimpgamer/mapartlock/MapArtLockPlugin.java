package nl.chimpgamer.mapartlock;

import nl.chimpgamer.mapartlock.config.Messages;
import nl.chimpgamer.mapartlock.config.Settings;
import nl.chimpgamer.mapartlock.item.MapDecorator;
import nl.chimpgamer.mapartlock.listener.HeldMapListener;
import nl.chimpgamer.mapartlock.listener.InventoryProtectionListener;
import nl.chimpgamer.mapartlock.listener.ItemFrameProtectionListener;
import nl.chimpgamer.mapartlock.listener.MenuListener;
import nl.chimpgamer.mapartlock.lock.LockRegistry;
import nl.chimpgamer.mapartlock.lock.MapLockService;
import nl.chimpgamer.mapartlock.lock.WorldLockStorage;
import nl.chimpgamer.mapartlock.menu.LockMenu;
import org.bukkit.World;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class MapArtLockPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();

        Settings settings = new Settings(getConfig());
        Messages messages = new Messages(this);

        LockRegistry registry = new LockRegistry(new WorldLockStorage(this, this::storageWorld));
        registry.load();
        getLogger().info(registry.size() + " map art lock(s) geladen.");

        MapDecorator decorator = new MapDecorator(this, settings, messages);
        MapLockService service = new MapLockService(settings, registry, decorator);
        LockMenu menu = new LockMenu(service, messages);

        registerCommand("mapartlock", "Beheer de bescherming van map art.", List.of(),
                new MapArtLockCommand(this, settings, messages, menu, service));

        PluginManager plugins = getServer().getPluginManager();
        plugins.registerEvents(new MenuListener(this, settings, service, menu, messages), this);
        plugins.registerEvents(new InventoryProtectionListener(settings, service, messages), this);
        plugins.registerEvents(new ItemFrameProtectionListener(settings, service, messages), this);
        plugins.registerEvents(new HeldMapListener(this, settings, service), this);
    }

    /**
     * The world whose container holds the locks. The first world is the overworld — the one
     * world a server always has, and the one whose data folder holds the map files themselves.
     */
    private World storageWorld() {
        List<World> worlds = getServer().getWorlds();
        return worlds.isEmpty() ? null : worlds.getFirst();
    }
}
