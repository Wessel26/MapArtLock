package nl.chimpgamer.mapartlock.listeners

import com.destroystokyo.paper.event.inventory.PrepareResultEvent
import nl.chimpgamer.mapartlock.MapArtLockPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.CrafterCraftEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent

class MapProtectionListener(private val plugin: MapArtLockPlugin) : Listener {

    @EventHandler
    fun onPrepareItemCraft(event: PrepareItemCraftEvent) {

    }

    @EventHandler
    fun onCrafterCraft(event: CrafterCraftEvent) {

    }

    @EventHandler
    fun onPrepareResult(event: PrepareResultEvent) {
        
    }
}