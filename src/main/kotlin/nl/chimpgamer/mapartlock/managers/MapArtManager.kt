package nl.chimpgamer.mapartlock.managers

import nl.chimpgamer.mapartlock.MapArtLockPlugin
import nl.chimpgamer.mapartlock.utils.UUIDDataType
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

class MapArtManager(private val plugin: MapArtLockPlugin) {

    fun isMap(itemStack: ItemStack?): Boolean {
        return itemStack != null && itemStack.type === Material.MAP
    }

    fun lockMapArt(itemStack: ItemStack, player: Player) {
        itemStack.editMeta { meta ->
            meta.persistentDataContainer.apply {
                set(plugin.mapArtLockedKey, PersistentDataType.BOOLEAN, true)
                set(plugin.mapArtOwnerKey, UUIDDataType, player.uniqueId)
            }
        }
    }

    fun unlockMapArt(itemStack: ItemStack) {
        itemStack.editMeta { meta ->
            meta.persistentDataContainer.remove(plugin.mapArtLockedKey)
            meta.persistentDataContainer.remove(plugin.mapArtOwnerKey)
        }
    }

    fun isMapArtLocked(itemStack: ItemStack): Boolean {
        return itemStack.persistentDataContainer.get(plugin.mapArtLockedKey, PersistentDataType.BOOLEAN) ?: false
    }

    fun getMapArtOwner(itemStack: ItemStack): UUID? {
        return itemStack.persistentDataContainer.get(plugin.mapArtOwnerKey, UUIDDataType)
    }

    fun isMapArtOwnedBy(itemStack: ItemStack, player: Player): Boolean {
        return getMapArtOwner(itemStack) == player.uniqueId
    }


}