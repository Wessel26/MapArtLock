package nl.chimpgamer.mapartlock.configurations

import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import nl.chimpgamer.mapartlock.MapArtLockPlugin

class MessagesConfig(plugin: MapArtLockPlugin) {
    val config: YamlDocument

    val mapArtLockSuccessfullyLocked: String get() = config.getString("map-art-lock.successfully-locked")
    val mapArtLockSuccessfullyUnlocked: String get() = config.getString("map-art-lock.successfully-unlocked")
    val mapArtLockNotHoldingMap: String get() = config.getString("map-art-lock.not-holding-map")
    val mapArtLockAlreadyLocked: String get() = config.getString("map-art-lock.already-locked")
    val mapArtLockAlreadyUnlocked: String get() = config.getString("map-art-lock.already-unlocked")

    init {
        val file = plugin.dataFolder.resolve("messages.yml")
        val inputStream = plugin.getResource("messages.yml")
        val loaderSettings = LoaderSettings.builder().setAutoUpdate(true).build()
        val updaterSettings = UpdaterSettings.builder().setVersioning(BasicVersioning("config-version")).build()
        config = if (inputStream != null) {
            YamlDocument.create(file, inputStream, GeneralSettings.DEFAULT, loaderSettings, DumperSettings.DEFAULT, updaterSettings)
        } else {
            YamlDocument.create(file, GeneralSettings.DEFAULT, loaderSettings, DumperSettings.DEFAULT, updaterSettings)
        }
    }
}