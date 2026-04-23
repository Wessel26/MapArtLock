package nl.chimpgamer.mapartlock.utils

import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import java.nio.ByteBuffer
import java.util.*
import kotlin.ByteArray

// We just need a singleton, so there's no need to allow instantiation
object UUIDDataType : PersistentDataType<ByteArray, UUID> {
    override fun getPrimitiveType(): Class<ByteArray> {
        return ByteArray::class.java
    }

    override fun getComplexType(): Class<UUID> {
        return UUID::class.java
    }

    override fun toPrimitive(complex: UUID, context: PersistentDataAdapterContext): ByteArray {
        val bb = ByteBuffer.allocate(Long.SIZE_BYTES * 2)
        bb.putLong(complex.mostSignificantBits)
        bb.putLong(complex.leastSignificantBits)
        return bb.array()
    }

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): UUID {
        val bb = ByteBuffer.wrap(primitive)
        val firstLong = bb.getLong()
        val secondLong = bb.getLong()
        return UUID(firstLong, secondLong)
    }
}