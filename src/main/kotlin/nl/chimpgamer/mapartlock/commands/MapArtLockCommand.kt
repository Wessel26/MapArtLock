package nl.chimpgamer.mapartlock.commands

import nl.chimpgamer.mapartlock.MapArtLockPlugin
import org.incendo.cloud.CommandManager
import org.incendo.cloud.paper.util.sender.PlayerSource
import org.incendo.cloud.paper.util.sender.Source

class MapArtLockCommand(private val plugin: MapArtLockPlugin) {

    fun registerCommands(commandManager: CommandManager<Source>, name: String, vararg aliases: String) {
        val basePermissions = "mapartlock.command.martartlock"

        val builder = commandManager.commandBuilder(name, *aliases)
            .permission(basePermissions)

        commandManager.command(builder
            .literal("about")
            .permission("$basePermissions.about")
            .handler { context ->
                val sender = context.sender().source()

                sender.sendRichMessage("<dark_gray>-------- <red>${plugin.name} <dark_gray>--------")
                sender.sendRichMessage("<red>Developers <dark_gray>» <gray>${plugin.authors.joinToString()}")
                sender.sendRichMessage("<red>Version <dark_gray>» <gray>${plugin.version}")
                sender.sendRichMessage("<red>Build Number <dark_gray>» <gray>${plugin.buildNumber}")
                sender.sendRichMessage("<red>Build Date <dark_gray>» <gray>${plugin.buildDate}")
            }
        )

        commandManager.command(builder
            .senderType(PlayerSource::class.java)
            .literal("lock", "protect")
            .permission("$basePermissions.lock")
            .handler { context ->
                val sender = context.sender().source()

                val mapArtManager = plugin.mapArtManager
                val itemInHand = sender.inventory.itemInMainHand
                if (!mapArtManager.isMap(itemInHand)) {
                    sender.sendMessage(plugin.messagesConfig.mapArtLockNotHoldingMap)
                    return@handler
                }

                if (mapArtManager.isMapArtLocked(itemInHand)) {
                    sender.sendMessage(plugin.messagesConfig.mapArtLockAlreadyLocked)
                    return@handler
                }

                
            }
        )

        commandManager.command(builder
            .senderType(PlayerSource::class.java)
            .literal("unlock", "unprotect")
            .permission("$basePermissions.unlock")
            .handler { context ->
                val sender = context.sender().source()
            }
        )
    }
}