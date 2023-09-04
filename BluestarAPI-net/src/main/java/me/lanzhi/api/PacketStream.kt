package me.lanzhi.api

import kotlinx.coroutines.*
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
    abstract suspend fun next(): Packet

    abstract suspend fun peek(): Packet

    abstract fun alive(): Boolean

    abstract fun close()

    abstract fun send(packet: Packet): Job

    @OptIn(DelicateCoroutinesApi::class)
    fun listen(listener: Consumer<Packet>) = GlobalScope.async {
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

    override suspend fun next(): Packet
    {
        val packet = this.packet ?: peek()
        this.packet = null
        return packet
    }

    override suspend fun peek(): Packet
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

    @OptIn(DelicateCoroutinesApi::class)
    override fun send(packet: Packet) = GlobalScope.launch {
        val bytes = coderGroup.encode(packet).let {
            output.writeShort(it.size)
            output.write(it)
        }
    }
}

class PacketConnection(
    private val socket: Socket,
    `in`: InputStream = socket.getInputStream(),
    `out`: OutputStream = socket.getOutputStream(),
    coderGroup: PacketCoderGroup = PacketCoderGroup()
) : DefaultPacketStream(`in`, `out`, coderGroup)
{
    override fun close()
    {
        socket.use {
            super.close()
        }
    }

    override fun alive(): Boolean
    {
        return socket.isConnected && super.alive()
    }
}