package nl.chimpgamer.mapartlock.config;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public final class MessageService {
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final JavaPlugin plugin;
    private final File file;
    private FileConfiguration defaults;
    private FileConfiguration messages;

    public MessageService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        reload();
    }

    public void reload() {
        this.defaults = loadDefaults();
        this.messages = YamlConfiguration.loadConfiguration(file);
        this.messages.setDefaults(defaults);
        addMissingDefaults();
    }

    public void send(CommandSender recipient, String key, TagResolver... resolvers) {
        recipient.sendMessage(miniMessage.deserialize(message(key), resolvers));
    }

    public String message(String key) {
        return messages.getString(key, defaults.getString(key, "<red>Missing message: " + key));
    }

    private FileConfiguration loadDefaults() {
        try (InputStream inputStream = plugin.getResource("messages.yml")) {
            if (inputStream == null) {
                return new YamlConfiguration();
            }
            return YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to load default messages.yml", exception);
            return new YamlConfiguration();
        }
    }

    private void addMissingDefaults() {
        boolean changed = false;
        for (String key : defaults.getKeys(true)) {
            if (!defaults.isConfigurationSection(key) && !messages.contains(key)) {
                messages.set(key, defaults.get(key));
                changed = true;
            }
        }

        if (!changed) {
            return;
        }

        try {
            messages.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to update messages.yml with missing defaults", exception);
        }
    }
}
