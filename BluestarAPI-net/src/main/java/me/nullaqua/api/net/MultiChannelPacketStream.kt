package me.nullaqua.api.net

import java.io.*
import java.util.*

class MultiChannelPacketStream(
    `in`: InputStream,
    `out`: OutputStream
) : PacketChannel()
{
    private val input = DataInputStream(`in`)
    private val output = DataOutputStream(`out`)
    private val channels = HashMap<UShort, PacketChannel>()
    private val tempChannels = HashMap<UShort, PacketChannel>()
    private val map = HashMap<UShort, UShort>()
    private var isClosed = false
    var onChannelCreate: ((PacketChannel, Boolean) -> Unit) = { _, _ -> }
    override val id: UShort
        get() = 0U

    init
    {
        channels[0U] = this
        for (i in 1..0xF) map[i.toUShort()] = i.toUShort()
        Thread()
        {
            while (!isClosed)
            {
                try
                {
                    val id = input.readUnsignedShort().toUShort()
                    if (id >= 0x10U || id == 0U.toUShort())
                    {
                        val length = input.readInt()
                        val bytes = ByteArray(length)
                        input.readFully(bytes)
                        Thread()
                        {
                            val packet = coderGroup.decode(bytes)
                            channels[id]?.onPacket(packet)
                        }.start()
                    }
                    else if (id == 1U.toUShort())
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
                            synchronized(map)
                            {
                                map[id0] = id1
                                (map as Object).notifyAll()
                            }
                            try
                            {
                                onChannelCreate(channel, true)
                            }
                            catch (e: Throwable)
                            {
                                e.printStackTrace()
                            }

                        }.start()
                    }
                    else if (id == 2U.toUShort())
                    {
                        val id0 = input.readUnsignedShort().toUShort()
                        val id1 = input.readUnsignedShort().toUShort()
                        synchronized(map)
                        {
                            map[id0] = id1
                            (map as Object).notifyAll()
                        }
                    }
                    else if (id == 3U.toUShort())
                    {
                        val id0 = input.readUnsignedShort().toUShort()
                        channels[id0]?.close()
                    }
                }
                catch (e: Throwable)
                {
                    isClosed = true
                    break
                }
            }
        }.start()
    }

    override fun alive() = !isClosed

    override fun close()
    {
        this.onClose(this)
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
        try
        {
            channel.onClose(channel)
        }
        finally
        {
            output.writeShort(3)
            output.writeShort(channel.id.toInt())
            channels.remove(channel.id)
            map.remove(channel.id)
        }
    }

    private fun getNewChannelID(): UShort
    {
        var id: UShort
        do
        {
            id = (Random().nextInt(0x10000-0x10)+0x10).toUShort()
        } while (channels.containsKey(id)||tempChannels.containsKey(id))
        return id
    }

    fun createChannel(): PacketChannel
    {
        val id = getNewChannelID()
        return createChannel(id)
    }

    private fun createChannel(channel: UShort): PacketChannel
    {
        tempChannels[channel] = DefaultPacketChannel(this, channel)
        output.writeShort(1)
        output.writeShort(channel.toInt())
        waitFor(channel)
        val ch = DefaultPacketChannel(this, channel)
        channels[channel] = ch
        try
        {
            onChannelCreate(ch, false)
        }
        catch (e: Throwable)
        {
            e.printStackTrace()
        }
        return ch
    }

    private fun waitFor(channel: UShort) = synchronized(map)
    {
        while (!map.containsKey(channel)) (map as Object).wait()
    }
}

sealed class PacketChannel : PacketStream()
{
    private val packets: MutableList<Packet> = LinkedList()
    var onClose: ((PacketChannel) -> Unit) = {}
    abstract val id: UShort

    override fun next(): Packet = synchronized(this)
    {
        while (packets.isEmpty() && alive()) (this as Object).wait()
        return if (alive()) packets.removeAt(0) else throw IOException("Channel closed")
    }

    override fun peek(): Packet = synchronized(this)
    {
        while (packets.isEmpty() && alive()) (this as Object).wait()
        return if (alive()) packets[0] else throw IOException("Channel closed")
    }

    abstract override fun alive(): Boolean

    abstract override fun close()

    abstract override fun send(packet: Packet)

    internal fun onPacket(packet: Packet) = synchronized(this)
    {
        packets.add(packet)
        (this as Object).notifyAll()
    }
}

class DefaultPacketChannel(
    private val stream: MultiChannelPacketStream,
    override val id: UShort
) : PacketChannel()
{
    override fun alive(): Boolean = stream.alive()

    override fun close() = stream.close(this)

    override fun send(packet: Packet) = stream.send(packet, this)
}