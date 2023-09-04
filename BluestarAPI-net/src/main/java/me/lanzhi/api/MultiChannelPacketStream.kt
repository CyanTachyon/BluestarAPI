package me.lanzhi.api

import kotlinx.coroutines.Job
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class MultiChannelPacketStream(
    `in`: InputStream,
    `out`: OutputStream,
    coderGroup: PacketCoderGroup = PacketCoderGroup()
) : Channel()
{
    private val input = DataInputStream(`in`)
    private val output = DataOutputStream(`out`)
    private var isClosed = false

    override fun alive(): Boolean
    {
        return !isClosed
    }

    override fun close()
    {

    }

    override fun send(packet: Packet): Job
    {
        TODO("Not yet implemented")
    }
}

sealed class Channel : PacketStream()
{
    private val packets: MutableList<Packet> = LinkedList()

    override suspend fun next(): Packet
    {
        return packets.removeFirst()
    }

    override suspend fun peek(): Packet
    {
        return packets.first()
    }

    abstract override fun alive(): Boolean

    abstract override fun close()

    abstract override fun send(packet: Packet): Job
}