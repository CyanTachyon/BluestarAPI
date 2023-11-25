package me.nullaqua.api

import me.nullaqua.api.collection.ByteVector
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class MultiChannelStream(`in`: InputStream, `out`: OutputStream)
{
    private val channels = HashMap<UShort, Channel>()
    private val map = HashMap<UShort, UShort>()
    private val wait = HashMap<UShort, Channel>()
    private val stream =PacketStream.create(`in`, `out`)
    private var isClosed = false

    init
    {
        stream.listen()
        {
            when (it)
            {
                is CreateChannel ->
                {
                    val channel = Channel(this, it.id)
                    synchronized(wait)
                    {
                        val id0 = it.id
                        val id1 = getNewChannelID()
                        channels[id0] = channel
                        stream.send(MapChannel(id0, id1))
                    }
                }
                is MapChannel    ->
                {
                    synchronized(map)
                    {
                        map[it.id0] = it.id1
                        (map as Object).notifyAll()
                    }
                }
                is CloseChannel  ->
                {
                    channels[it.id]?.close()
                    channels.remove(it.id)
                }
                is DataPacket    ->
                {
                    channels[map[it.id] ?: throw IllegalArgumentException("Channel ${it.id} not found")]?.onReceive(
                        it.data)
                }
            }
        }
    }

    operator fun get(id: UShort) = channels[id]

    fun getOrThrow(id: UShort) = channels[id] ?: throw IllegalArgumentException("Channel $id not found")

    fun getOrNew(id: UShort) = channels[id] ?: createChannel(id)

    fun close()
    {
        isClosed = true
        channels.values.forEach { it.close() }
        stream.close()
    }

    fun createChannel(): Channel= createChannel(getNewChannelID())

    private fun createChannel(id: UShort): Channel
    {
        val channel = Channel(this, id)
        synchronized(wait)
        {
            wait[id] = channel
            waitFor(id)
            channels[id] = channel
            wait.remove(id)
        }
        return channel
    }

    private fun waitFor(id: UShort) = synchronized(wait)
    {
        while (!map.containsKey(id)&&!isClosed)
        {
            (map as Object).wait()
        }
        if (isClosed) throw IllegalStateException("Stream is closed")
    }

    private fun getNewChannelID(): UShort
    {
        var id: UShort
        do
        {
            id = (Random().nextInt(0x10000-0x10)+0x10).toUShort()
        } while (channels.containsKey(id)||wait.containsKey(id))
        return id
    }

    fun send(channel: Channel, data: ByteArray)
    {
        stream.send(DataPacket(map[channel.id] ?: throw IllegalArgumentException("Channel ${channel.id} not found"), data))
    }

    companion object
    {
        @JvmStatic
        val coderGroup = PacketCoderGroup()

        init
        {
            coderGroup.addCoder(DataPacket.coder)
            coderGroup.addCoder(CreateChannel.coder)
            coderGroup.addCoder(MapChannel.coder)
            coderGroup.addCoder(CloseChannel.coder)
        }
    }

    private class CreateChannel(val id: UShort = 0U) : Packet()
    {
        companion object
        {
            @JvmStatic
            val coder = object : PacketCoder<CreateChannel>(1, CreateChannel::class.java)
            {
                override fun encode(packet: CreateChannel, out: DataOutputStream) = out.writeShort(packet.id.toInt())

                override fun decode(input: DataInputStream) = CreateChannel(input.readUnsignedShort().toUShort())
            }
        }
    }

    private class MapChannel(
        val id0: UShort = 0U,
        val id1: UShort = 0U
    ) : Packet()
    {
        companion object
        {
            @JvmStatic
            val coder = object : PacketCoder<MapChannel>(2, MapChannel::class.java)
            {
                override fun encode(packet: MapChannel, out: DataOutputStream)
                {
                    out.writeShort(packet.id0.toInt())
                    out.writeShort(packet.id1.toInt())
                }

                override fun decode(input: DataInputStream): MapChannel=
                    MapChannel(input.readUnsignedShort().toUShort(), input.readUnsignedShort().toUShort())
            }
        }
    }

    private class CloseChannel(val id: UShort = 0U) : Packet()
    {
        companion object
        {
            @JvmStatic
            val coder = object : PacketCoder<CloseChannel>(3, CloseChannel::class.java)
            {
                override fun encode(packet: CloseChannel, out: DataOutputStream) = out.writeShort(packet.id.toInt())

                override fun decode(input: DataInputStream): CloseChannel=
                    CloseChannel(input.readUnsignedShort().toUShort())
            }
        }
    }

    private class DataPacket(
        val id: UShort = 0U,
        val data: ByteArray = ByteArray(0)
    ) : Packet()
    {

        companion object
        {
            @JvmStatic
            val coder = object : PacketCoder<DataPacket>(0, DataPacket::class.java)
            {
                override fun encode(packet: DataPacket, out: DataOutputStream)
                {
                    out.writeShort(packet.id.toInt())
                    out.writeInt(packet.data.size)
                    out.write(packet.data)
                }

                override fun decode(input: DataInputStream): DataPacket=
                    DataPacket(input.readUnsignedShort().toUShort(),
                               ByteArray(input.readInt()).apply { input.readFully(this) })
            }
        }
    }


}
class Channel(private val father: MultiChannelStream, internal val id: UShort)
{
    private val inputBuffer = LinkedList<Byte>()
    private val outputBuffer = ByteVector()
    private var isClosed = false
    val inputStream = object : InputStream()
    {
        override fun read(): Int
        {
            while (!isClosed)
            {
                synchronized(inputBuffer)
                {
                    if (inputBuffer.isNotEmpty()) return inputBuffer.removeFirst().toInt()
                }
                (inputBuffer as Object).wait()
            }
            return -1
        }
    }
    val outputStream = object : OutputStream()
    {
        override fun write(b: Int) = synchronized(outputBuffer)
        {
            if (!isClosed) outputBuffer.add(b.toByte())
        }

        override fun flush() = this@Channel.flush()
    }

    private fun flush() = synchronized(outputBuffer)
    {
        if (outputBuffer.isNotEmpty())
        {
            father.send(this, outputBuffer.toByteArray())
            outputBuffer.clear()
        }
    }

    fun close()
    {
        flush()
        isClosed = true
        (inputBuffer as Object).notifyAll()
    }

    internal fun onReceive(data: ByteArray)
    {
        synchronized(inputBuffer)
        {
            inputBuffer.addAll(data.toList())
            (inputBuffer as Object).notifyAll()
        }
    }
}
