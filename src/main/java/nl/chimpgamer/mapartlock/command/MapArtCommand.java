package nl.chimpgamer.mapartlock.command;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import nl.chimpgamer.mapartlock.config.MessageService;
import nl.chimpgamer.mapartlock.config.PluginSettings;
import nl.chimpgamer.mapartlock.menu.MapLockMenu;
import nl.chimpgamer.mapartlock.service.MapLockService;
import nl.chimpgamer.mapartlock.permission.MapLockPermissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public final class MapArtCommand implements BasicCommand {
    private final JavaPlugin plugin;
    private final PluginSettings settings;
    private final MapLockMenu menu;
    private final MapLockService mapLockService;
    private final MessageService messages;

    public MapArtCommand(JavaPlugin plugin, PluginSettings settings, MapLockMenu menu, MapLockService mapLockService, MessageService messages) {
        this.plugin = plugin;
        this.settings = settings;
        this.menu = menu;
        this.mapLockService = mapLockService;
        this.messages = messages;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();
        if (args.length > 0) {
            handleSubCommand(sender, args);
            return;
        }

        if (!(sender instanceof Player player)) {
            messages.send(sender, "must_hold_map");
            return;
        }

        if (!MapLockPermissions.canOpenMapArt(player)) {
            messages.send(player, "no_permission");
            return;
        }

        menu.open(player);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return MapLockPermissions.canOpenMapArt(sender)
                || MapLockPermissions.canReload(sender)
                || MapLockPermissions.canViewVersion(sender);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (args.length <= 1) {
            return List.of("reload", "version").stream()
                    .filter(subCommand -> canSeeSuggestion(source.getSender(), subCommand))
                    .toList();
        }
        return List.of();
    }

    @Override
    public @Nullable String permission() {
        return null;
    }


    private boolean canSeeSuggestion(CommandSender sender, String subCommand) {
        if (subCommand.equalsIgnoreCase("reload")) {
            return MapLockPermissions.canReload(sender);
        }
        if (subCommand.equalsIgnoreCase("version")) {
            return MapLockPermissions.canViewVersion(sender);
        }
        return false;
    }

    private void handleSubCommand(CommandSender sender, String[] args) {
        String subCommand = args[0];
        if (subCommand.equalsIgnoreCase("reload")) {
            reload(sender);
            return;
        }

        if (subCommand.equalsIgnoreCase("version")) {
            version(sender);
            return;
        }

        messages.send(sender, "usage");
    }

    private void reload(CommandSender sender) {
        if (!MapLockPermissions.canReload(sender)) {
            messages.send(sender, "no_permission");
            return;
        }

        plugin.reloadConfig();
        settings.reload(plugin.getConfig());
        messages.reload();
        messages.send(sender, "config_reloaded");
    }


    private void version(CommandSender sender) {
        if (!MapLockPermissions.canViewVersion(sender)) {
            messages.send(sender, "no_permission");
            return;
        }

        messages.send(sender, "version", net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("version", plugin.getPluginMeta().getVersion()));
    }


}
