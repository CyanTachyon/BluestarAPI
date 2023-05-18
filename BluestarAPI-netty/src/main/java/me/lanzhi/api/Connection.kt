package me.lanzhi.api

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import kotlin.math.max

typealias NettyChannel = io.netty.channel.Channel

class Connection(private val nettyChnl: NettyChannel) : Channel()
{
    //频道占用情况
    private val map= mutableMapOf<UShort, Channel>()
    val handlers = mutableListOf<ConnectionHandler>()
    override val id: UShort = 0U
    override val connection: Connection = this

    init
    {
        nettyChnl.pipeline().addLast("receive", object : io.netty.channel.ChannelInboundHandlerAdapter()
        {
            override fun channelRead(ctx: io.netty.channel.ChannelHandlerContext?, msg: Any?)
            {
                if (msg !is ByteBuf) return
                val id = msg.readUnsignedShort().toUShort()
                if (id > 0U && id < 16U)
                {
                    when (id)
                    {
                        1.toUShort() -> createChannel(msg.readUnsignedShort().toUShort(), ChannelReason.REMOTE)
                        2.toUShort() -> closeChannel(msg.readUnsignedShort().toUShort(), ChannelReason.REMOTE)
                        3.toUShort() -> send(reserve(4U), msg.readBytes(msg.readableBytes()))
                        4.toUShort() ->
                        {
                            val time=msg.readLong()
                            while (pendingPing.isNotEmpty() &&pendingPing.min()<time)
                            {
                                pendingPing.remove(pendingPing.min())
                                ping=System.currentTimeMillis()-time
                            }
                        }
                        else -> msg.release()
                    }
                }
                else
                {
                    val channel = (if (id == 0U.toUShort()) this@Connection else map[id]) ?: return
                    channel.onMessage(msg.readBytes(msg.readableBytes()))
                }
            }
        })

        for (i in 0 .. 15) map[i.toUShort()] = reserve(i.toUShort())
    }

    fun send(channel: Channel, data: Any)
    {
        if (channel.connection != this) throw IllegalArgumentException("channel not in this connection")
        val data1 = if (channel !is ReservedChannel)channel.onSend(data) else listOf(data)
        data1.forEach { //处理所有netty支持的数据类型
            when (it)
            {
                is ByteBuf ->
                {
                    val buf = ByteBufAllocator.DEFAULT.buffer(it.readableBytes())
                    buf.writeBytes(it)
                    send0(buf)
                }

                is ByteArray ->
                {
                    val buf = ByteBufAllocator.DEFAULT.buffer(it.size)
                    buf.writeBytes(it)
                    send0(buf)
                }

                is String ->
                {
                    val buf = ByteBufAllocator.DEFAULT.buffer(it.length)
                    buf.writeCharSequence(it, Charsets.UTF_8)
                    send0(buf)
                }

                else ->
                {
                    throw IllegalArgumentException("unsupported data type")
                }
            }
        }
    }

    private fun send0(data: ByteBuf)
    {
        nettyChnl.writeAndFlush(data)
    }

    override fun send(data: Any) = send(this, data)

    override fun close()
    {
        nettyChnl.close()
    }

    override fun alive(): Boolean = nettyChnl.isOpen

    fun createChannel(): Channel = createChannel(randomAvailableChannelId(), ChannelReason.LOCAL)

    fun createChannel(id: UShort): Channel = createChannel(id, ChannelReason.LOCAL)

    private fun createChannel(id: UShort, channelReason: ChannelReason): Channel
    {
        if (map.containsKey(id) || id < 16U) throw IllegalArgumentException("channel id already in use")
        val channel = DeafultChannel(id, this)
        map[id] = channel
        if (channelReason == ChannelReason.LOCAL)
        {
            val buf = ByteBufAllocator.DEFAULT.buffer(4)
            buf.writeShort(1)
            buf.writeShort(id.toInt())
            send0(buf)
        }
        handlers.forEach { it.onChannelCreated(channel, channelReason) }
        return channel
    }

    fun closeChannel(channel: Channel) = closeChannel(channel, ChannelReason.LOCAL)

    fun closeChannel(id: UShort, channelReason: ChannelReason) =
            closeChannel(get(id) ?: throw IllegalArgumentException("channel not found"), channelReason)

    private fun closeChannel(channel: Channel, channelReason: ChannelReason)
    {
        if (channel.connection != this) throw IllegalArgumentException("channel not in this connection")
        if (!channel.alive()) throw IllegalArgumentException("channel already closed")
        map.remove(channel.id)
        if (channelReason == ChannelReason.LOCAL)
        {
            val buf = ByteBufAllocator.DEFAULT.buffer(4)
            buf.writeShort(2)
            buf.writeShort(channel.id.toInt())
        }
        handlers.forEach { it.onChannelClosed(channel, channelReason) }
    }

    operator fun get(id: UShort): Channel?
    {
        return map[id]
    }

    //获取最小可用的频道id
    fun getMinAvailableChannelId(start:UShort=16U): UShort
    {
        var id = start
        while (map.containsKey(id)&&map.size<65536)id++
        if (map.size>=65536)throw IllegalStateException("no available channel id")
        return id
    }

    fun randomAvailableChannelId(): UShort
    {
        return getMinAvailableChannelId((UShort.MIN_VALUE..UShort.MAX_VALUE).random().toUShort())
    }

    fun getChannelCount():Int=map.size
    fun getChannelIds():Set<UShort> = map.keys
    fun getChannels():Set<Channel> = map.values.toSet()
    val allChannelIds:Set<UShort> by lazy { (0U..65535U).map { it.toUShort() }.toSet() }
    fun getAvailableChannelIds():Set<UShort> = allChannelIds.minus(getChannelIds())
    fun getAvailableChannelCount():Int = 65536-getChannelCount()

    /* ping */

    //最后一次ping的结果
    private var ping:Long=0
    //发送但未收到pong的ping
    private val pendingPing= mutableSetOf<Long>()

    fun ping():Long
    {
        //如果没有未收到pong的ping，则返回最后一次ping的结果
        if (pendingPing.isEmpty())return ping
        //如果有未收到pong的ping，则返回最早的max(最早未收到pong的ping与当前时间差,最后一次ping的结果)
        return max(System.currentTimeMillis()- pendingPing.min(), ping)
    }
}

interface ConnectionHandler
{
    fun onChannelCreated(channel: Channel, channelReason: ChannelReason)=Unit
    fun onChannelClosed(channel: Channel, channelReason: ChannelReason)=Unit
}

enum class ChannelReason
{
    REMOTE, LOCAL
}