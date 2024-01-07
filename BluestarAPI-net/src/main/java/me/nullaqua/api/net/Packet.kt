package me.nullaqua.api.net

import me.nullaqua.api.util.LoggerUtils
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.logging.Logger

val logger = LoggerUtils(Logger.getLogger("PacketStream"))

/**
 * 一个包
 */
abstract class Packet

/**
 * 一个包编码器
 */
abstract class PacketCoder<T: Packet> constructor(val id: UByte, val packetClass: Class<T>)
{
    abstract fun encode(packet: T, out: DataOutputStream)
    abstract fun decode(input: DataInputStream): T
}

/**
 * 由若干个包编码器组成的编码器组
 */
class PacketCoderGroup
{
    private val map: MutableMap<Class<*>, PacketCoder<*>> = mutableMapOf()
    private val coders: Array<PacketCoder<*>?> = Array(UByte.MAX_VALUE.toInt()+1) { null }
    fun addCoder(coder: PacketCoder<*>)
    {
        map[coder.packetClass] = coder
        coders[coder.id.toInt()] = coder
    }

    fun removeCoder(coder: PacketCoder<*>)
    {
        map.remove(coder.packetClass)
        coders[coder.id.toInt()] = null
    }

    fun encode(packet: Packet): ByteArray
    {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val output = DataOutputStream(byteArrayOutputStream)
        this[packet.javaClass]?.apply()
        {
            output.writeByte(this.id.toInt())
            this.encode(packet, output)
        } ?: throw Exception("No coder found for packet ${packet.javaClass.name}")
        return byteArrayOutputStream.toByteArray()
    }

    fun decode(bytes: ByteArray): Packet
    {
        val dataInput = DataInputStream(bytes.inputStream())
        val id = dataInput.readUnsignedByte().toUByte()
        val coder = this[id] ?: run()
        {
            logger.warning("No coder found for packet id $id")
            throw Exception("No coder found for packet id $id")
        }
        return coder.decode(dataInput)
    }

    operator fun get(id: UByte): PacketCoder<*>? = coders[id.toInt()]
    operator fun <T: Packet> get(arg: Class<in T>): PacketCoder<in T>?
    {
        var clazz: Class<in T> = arg
        while (clazz!=Packet::class.java)
        {
            var coder = map[clazz]
            if (coder!=null) return coder as PacketCoder<in T>
            for (i in clazz.interfaces)
            {
                coder = map[i]
                if (coder!=null) return coder as PacketCoder<in T>
            }
            clazz = clazz.superclass
        }
        return null
    }
}