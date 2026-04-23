package nl.chimpgamer.mapartlock.managers

import nl.chimpgamer.mapartlock.MapArtLockPlugin
import nl.chimpgamer.mapartlock.commands.MapArtLockCommand
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper
import org.incendo.cloud.paper.util.sender.Source
import java.util.logging.Level

class CloudCommandManager(private val plugin: MapArtLockPlugin) {
    private lateinit var paperCommandManager: PaperCommandManager<Source>

    fun initialize() {
        try {
            paperCommandManager = PaperCommandManager.builder(PaperSimpleSenderMapper.simpleSenderMapper())
                .executionCoordinator(ExecutionCoordinator.asyncCoordinator())
                .buildOnEnable(plugin)
        } catch (ex: Exception) {
            plugin.logger.log(Level.SEVERE, "Failed to initialize the command manager", ex)
        }
    }

    fun loadCommands() {
        MapArtLockCommand(plugin).registerCommands(paperCommandManager, "martartlock")
    }
}