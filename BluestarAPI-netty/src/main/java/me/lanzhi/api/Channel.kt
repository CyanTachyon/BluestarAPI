package me.lanzhi.api

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator

sealed class Channel
{
    abstract val id: UShort
    abstract val connection: Connection

    val channelHandlers: MutableList<ChannelHandler<Any?>> = mutableListOf()

    abstract fun send(data: Any)
    fun send(data: ByteArray) = send(ByteBufAllocator.DEFAULT.buffer().writeBytes(data))

    abstract fun close()

    abstract fun alive(): Boolean

    internal fun onMessage(buf: ByteBuf)
    {
        var list = listOf<Any?>(buf)
        channelHandlers.forEach {handler->
            val temp = mutableListOf<Any?>()
            list.forEach{o->
                try
                {
                    temp.addAll(handler.onReceive(this@Channel, o))
                }
                catch (_: Exception)
                {
                }
            }
            list = temp
        }
    }

    internal fun onSend(o: Any): List<Any?>
    {
        var list = listOf<Any?>(o)
        channelHandlers.forEach {
            val temp = mutableListOf<Any?>()
            try
            {
                list.forEach { i -> temp.addAll(it.onSend(this@Channel, i)) }
            } catch (_: Exception)
            {
            }
            list = temp
        }
        return list
    }
}

//保留通道
class ReservedChannel(override val id: UShort, override val connection: Connection) : Channel()
{
    override fun send(data: Any) = throw UnsupportedOperationException("this channel is reserved")

    override fun close() = throw UnsupportedOperationException("this channel is reserved")

    override fun alive(): Boolean = true
}
fun Connection.reserve(id: UShort): ReservedChannel
{
    return ReservedChannel(id, this)
}

class DeafultChannel(override val id: UShort, override val connection: Connection) : Channel()
{
    override fun send(data: Any)
    {
        connection.send(this, data)
    }

    override fun close()
    {
        connection.closeChannel(this)
    }

    override fun alive(): Boolean
    {
        return connection[id] == this
    }
}

interface ChannelHandler<in T:Any?>
{
    fun onReceive(channel: Channel, data: T): List<*> = listOf(data)
    fun onSend(channel: Channel, data: T):List<*> = listOf(data)
}

class TransformChannelHandler(private val otherChannel:Channel):ChannelHandler<ByteBuf>
{
    override fun onReceive(channel: Channel, data: ByteBuf): List<ByteBuf>
    {
        otherChannel.send(data)
        return emptyList()
    }
}

fun Channel.transform(otherChannel: Channel)
{
    channelHandlers.clear()
    channelHandlers.add(TransformChannelHandler(otherChannel)as ChannelHandler<Any?>)
}