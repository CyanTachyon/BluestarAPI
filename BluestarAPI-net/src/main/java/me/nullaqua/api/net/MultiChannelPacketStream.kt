package me.nullaqua.api.net

import me.nullaqua.api.kotlin.lock
import me.nullaqua.api.kotlin.notifyAll
import me.nullaqua.api.kotlin.wait
import java.io.*
import java.net.Socket
import java.util.*
import java.util.stream.Stream

open class MultiChannelPacketStream(
    `in`: InputStream,
    `out`: OutputStream
): PacketChannel()
{
    private val input = DataInputStream(`in`)
    private val output = DataOutputStream(`out`)
    private val channels = HashMap<UShort, PacketChannel>()
    private val tempChannels = HashMap<UShort, PacketChannel>()
    private val map = HashMap<UShort, UShort>()
    private var isClosed = false
    var onChannelCreate: ((PacketChannel, Boolean)->Unit) = { _, _ -> }
    override val id: UShort
        get() = 0U
    override val stream: MultiChannelPacketStream
        get() = this

    init
    {
        channels[0U] = this
        for (i in 0x01U..0x0FU) map[i.toUShort()] = i.toUShort()
        Thread { while (!isClosed) runCatching(::parse).onFailure { close();return@Thread } }.start()
    }

    private fun parse()
    {
        val id = input.readUnsignedShort().toUShort()
        if (id>=0x10U||id==0U.toUShort())
        {
            val length = input.readInt()
            val bytes = ByteArray(length)
            input.readFully(bytes)
            Thread { channels[id]?.addPacket?.invoke(coderGroup.decode(bytes)) }.start()
        }
        else if (id==1U.toUShort())
        {
            val id1 = input.readUnsignedShort().toUShort()
            val id0 = getNewChannelID()
            val channel = DefaultPacketChannel(this, id0)
            channels[id0] = channel
            Thread()
            {
                output.writeShort(2)
                output.writeShort(id1.toInt())
                output.writeShort(id0.toInt())
                map.lock()
                {
                    map[id0] = id1
                    map.notifyAll()
                }
                runCatching { onChannelCreate(channel, true) }.exceptionOrNull()?.printStackTrace()
            }.start()
        }
        else if (id==2U.toUShort())
        {
            val id0 = input.readUnsignedShort().toUShort()
            val id1 = input.readUnsignedShort().toUShort()
            map.lock()
            {
                map[id0] = id1
                map.notifyAll()
            }
        }
        else if (id==3U.toUShort())
        {
            channels[input.readUnsignedShort().toUShort()]?.close()
        }
    }

    override fun alive() = !isClosed
    override fun close()
    {
        this.onClose()
        input.close()
        output.close()
    }

    override fun send(packet: Packet) = send(packet, 0U)
    fun send(packet: Packet, channel: PacketChannel) = send(packet, channel.id)
    private fun send(packet: Packet, channel: UShort)
    {
        val bytes = coderGroup.encode(packet)
        send(channel, bytes)
    }

    private fun send(channel: UShort, bytes: ByteArray)
    {
        output.writeShort(map[channel]!!.toInt())
        output.writeInt(bytes.size)
        output.write(bytes)
    }

    fun close(channel: PacketChannel)
    {
        if (!channel.alive()) return
        if (channel.stream!=this) return
        if (channel.id==0U.toUShort())
        {
            this.close()
            return
        }
        if (channel.id<0x10U) return
        runCatching(channel.pipe::close)
        runCatching { channel.onClose.invoke(channel) }
        output.writeShort(3)
        map[channel.id]?.let { output.writeShort(it.toInt()) }
        channels.remove(channel.id)
        map.remove(channel.id)
    }

    private fun getNewChannelID(): UShort
    {
        var id: UShort
        do
        {
            id = (Random().nextInt(0x10000-0x10)+0x10).toUShort()
        }
        while (channels.containsKey(id)||tempChannels.containsKey(id))
        return id
    }

    fun createChannel(): PacketChannel = createChannel(getNewChannelID())
    private fun createChannel(channel: UShort): PacketChannel
    {
        tempChannels[channel] = DefaultPacketChannel(this, channel)
        output.writeShort(1)
        output.writeShort(channel.toInt())
        waitFor(channel)
        val ch = DefaultPacketChannel(this, channel)
        channels[channel] = ch
        runCatching { onChannelCreate(ch, false) }.exceptionOrNull()?.printStackTrace()
        return ch
    }

    private fun waitFor(channel: UShort) = map.lock()
    {
        while (!map.containsKey(channel)) map.wait()
    }
}

sealed class PacketChannel: PacketStream<PacketChannel>()
{
    abstract val id: UShort
    final override val pipe: PacketPipe
    internal val addPacket: (Packet)->Unit

    init
    {
        val (pipe, addPacket) = PacketPipe.create()
        this.pipe = pipe
        this.addPacket = addPacket
    }

    abstract override fun alive(): Boolean
    abstract override fun close()
    abstract override fun send(packet: Packet)
    abstract val stream: MultiChannelPacketStream
}

class DefaultPacketChannel(
    override val stream: MultiChannelPacketStream,
    override val id: UShort
): PacketChannel()
{
    override fun alive(): Boolean = stream.alive()
    override fun close() = stream.close(this)
    override fun send(packet: Packet) = stream.send(packet, this)
}

class MultiChannelPacketConnection(
    val socket: Socket,
    input: InputStream = socket.getInputStream(),
    output: OutputStream = socket.getOutputStream()
): MultiChannelPacketStream(input, output)
{
    override fun alive(): Boolean = super.alive()&&!socket.isClosed
    override fun close() = runCatching(socket::close).run { super.close() }
}