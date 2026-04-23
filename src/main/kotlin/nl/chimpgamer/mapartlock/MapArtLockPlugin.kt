package nl.chimpgamer.mapartlock

import nl.chimpgamer.mapartlock.configurations.MessagesConfig
import nl.chimpgamer.mapartlock.configurations.SettingsConfig
import nl.chimpgamer.mapartlock.listeners.MapProtectionListener
import nl.chimpgamer.mapartlock.managers.CloudCommandManager
import nl.chimpgamer.mapartlock.managers.MapArtManager
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin

class MapArtLockPlugin : JavaPlugin() {
    val settingsConfig = SettingsConfig(this)
    val messagesConfig = MessagesConfig(this)
    val cloudCommandManager = CloudCommandManager(this)
    val mapArtManager = MapArtManager(this)

    val mapArtLockedKey = NamespacedKey(this, "map-art-locked")
    val mapArtOwnerKey = NamespacedKey(this, "map-art-owner")

    var buildNumber: String = ""
    var buildDate: String = ""

    override fun onLoad() {
        loadPluginInfo()
    }

    override fun onEnable() {
        // Plugin startup logic

        cloudCommandManager.initialize()
        cloudCommandManager.loadCommands()

        server.pluginManager.registerEvents(MapProtectionListener(this), this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    private fun loadPluginInfo() {
        getResource("plugin.yml")?.reader()?.let { reader ->
            val pluginYml = YamlConfiguration.loadConfiguration(reader)
            buildNumber = pluginYml.getString("build-number") ?: ""
            buildDate = pluginYml.getString("build-date") ?: ""
        }
    }

    @Suppress("DEPRECATION")
    val version get() = description.version

    @Suppress("DEPRECATION")
    val authors: List<String> get() = description.authors
}
