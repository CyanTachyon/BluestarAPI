package me.nullaqua.api

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
abstract class PacketCoder<T : Packet> constructor(val id: Int, val packetClass: Class<T>)
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
        coders[coder.id] = coder
    }

    fun removeCoder(coder: PacketCoder<*>)
    {
        map.remove(coder.packetClass)
        coders[coder.id] = null
    }

    fun encode(packet: Packet): ByteArray
    {
        var c: Class<*> = packet.javaClass
        while (c != Packet::class.java)
        {
            var coder = map[c]
            if (coder != null)
            {
                val o = ByteArrayOutputStream()
                val out = DataOutputStream(o)
                out.write(coder.id)
                (coder as PacketCoder<Packet>).encode(packet, out)
                return o.toByteArray()
            }
            for (i in c.interfaces)
            {
                coder = map[i]
                if (coder != null)
                {
                    val o = ByteArrayOutputStream()
                    val out = DataOutputStream(o)
                    out.write(coder.id)
                    (coder as PacketCoder<Packet>).encode(packet, out)
                    return o.toByteArray()
                }
            }
            c = c.superclass
        }
        throw Exception("No coder found for packet ${packet.javaClass.name}")
    }

    fun decode(bytes: ByteArray): Packet
    {
        val dataInput= DataInputStream(bytes.inputStream())
        val id = dataInput.readUnsignedByte()
        val coder = coders[id] ?: run()
        {
            logger.warning("No coder found for packet id $id")
            throw Exception("No coder found for packet id $id")
        }
        return coder.decode(dataInput)
    }
}