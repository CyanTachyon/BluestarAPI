package me.nullaqua.api.net

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.util.function.Consumer

/**
 * 一个数据包传输流
 */
abstract class PacketStream(var coderGroup: PacketCoderGroup = PacketCoderGroup())
{
    abstract fun next(): Packet

    abstract fun peek(): Packet

    abstract fun alive(): Boolean

    abstract fun close()

    abstract fun send(packet: Packet)

    fun listen(listener: Consumer<Packet>)
    {
        while (alive())
        {
            try
            {
                listener.accept(next())
            }
            catch (_: Exception)
            {
            }
        }
    }

    companion object
    {
        fun create(
            `in`: InputStream,
            `out`: OutputStream,
            coderGroup: PacketCoderGroup = PacketCoderGroup()
        ): PacketStream
        {
            return DefaultPacketStream(`in`, `out`, coderGroup)
        }
    }
}

/**
 * 默认形式的数据包传输流,由一个输入流和一个输出流组成
 */
open class DefaultPacketStream(
    `in`: InputStream,
    `out`: OutputStream,
    coderGroup: PacketCoderGroup = PacketCoderGroup()
) : PacketStream(coderGroup)
{
    private val input = DataInputStream(`in`)
    private val output = DataOutputStream(`out`)
    private var isClosed = false
    private var packet: Packet? = null

    override fun next(): Packet
    {
        val packet = this.packet ?: peek()
        this.packet = null
        return packet
    }

    override fun peek(): Packet
    {
        if (packet != null) return packet!!
        val length = input.readUnsignedShort()
        val bytes = ByteArray(length)
        input.readFully(bytes)
        packet = coderGroup.decode(bytes)
        return packet as Packet
    }

    override fun alive(): Boolean
    {
        return !isClosed
    }

    override fun close()
    {
        isClosed = true
        input.close()
        output.close()
    }

    override fun send(packet: Packet) = coderGroup.encode(packet).let()
    {
        output.writeInt(it.size)
        output.write(it)
    }
}

class PacketConnection(
    private val socket: Socket,
    `in`: InputStream = socket.getInputStream(),
    `out`: OutputStream = socket.getOutputStream(),
    coderGroup: PacketCoderGroup = PacketCoderGroup()
) : DefaultPacketStream(`in`, `out`, coderGroup)
{
    override fun close() = socket.use { super.close() }

    override fun alive() = socket.isConnected && super.alive()
}