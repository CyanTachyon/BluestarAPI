package me.lanzhi.api

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOption
import me.lanzhi.api.util.LoggerUtils
import java.util.*
import java.util.logging.Logger
import kotlin.math.log
import kotlin.math.max
import kotlin.math.min

typealias NettyChannel = io.netty.channel.Channel

var logger: LoggerUtils = LoggerUtils(Logger.getLogger("BluestarAPI"))
internal fun <T> runSave(block: () -> T): T? = try
{
    block()
}
catch (_: Exception)
{
    null
}

class Connection private constructor(private val nettyChnl: NettyChannel) : Channel()
{
    companion object
    {
        //连接池,防止重复创建连接,同时防止连接不会被回收
        private val map = WeakHashMap<NettyChannel, Connection>()

        @JvmStatic
        @Synchronized
        fun of(nettyChnl: NettyChannel): Connection
        {
            val connection = map[nettyChnl]
            if (connection != null) return connection
            val newConnection = Connection(nettyChnl)
            map[nettyChnl] = newConnection
            //设置ChannelOption.RCVBUF_ALLOCATOR

            return newConnection
        }

        fun NettyChannel.toConnection(): Connection = of(this)
    }

    private val map = mutableMapOf<UShort, Channel>()
    val connectionHandlers = mutableListOf<ConnectionHandler>()
    override val id: UShort = 0U
    override val connection: Connection = this

    init
    {
        nettyChnl.pipeline().addLast("receive", object : io.netty.channel.SimpleChannelInboundHandler<ByteBuf>()
        {
            override fun channelRead0(ctx: ChannelHandlerContext?, msg: ByteBuf)
            {
                val id = msg.readUnsignedShort().toUShort()
                logger.info("Received packet with id $id")
                if (id > 0U && id < 16U)
                {
                    when (id)
                    {
                        1.toUShort() -> createChannel(msg.readUnsignedShort().toUShort(), ChannelReason.REMOTE)
                        2.toUShort() -> closeChannel(msg.readUnsignedShort().toUShort(), ChannelReason.REMOTE)
                    }
                }
                else
                {
                    val channel = (if (id == 0U.toUShort()) this@Connection else map[id]) ?: return
                    val len = msg.readUnsignedShort()
                    logger.info("received packet with length $len from channel ${channel.id}")
                    val byteBuf = ByteBufAllocator.DEFAULT.buffer(len)
                    msg.readBytes(byteBuf)
                    channel.onMessage(byteBuf)
                }
            }
        })

        for (i in 0..15) map[i.toUShort()] = reserve(i.toUShort())
    }

    fun send(channel: Channel, data: Any)
    {
        if (channel.connection != this || !channel.alive()) return
        val data1 = if (channel !is ReservedChannel) channel.onSend(data)
        else if (data is ByteBuf) data
        else throw IllegalArgumentException("data must be ByteBuf")


        val buf = ByteBufAllocator.DEFAULT.buffer(4 + data1.readableBytes())
        buf.writeShort(channel.id.toInt())
        buf.writeShort(data1.readableBytes())
        buf.writeBytes(data1)
        send0(buf)
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
        connectionHandlers.forEach { runSave { it.onChannelCreated(channel, channelReason) } }
        return channel
    }

    fun closeChannel(channel: Channel) = closeChannel(channel, ChannelReason.LOCAL)

    fun closeChannel(id: UShort, channelReason: ChannelReason) =
        closeChannel(get(id) ?: throw IllegalArgumentException("channel not found"), channelReason)

    private fun closeChannel(channel: Channel, channelReason: ChannelReason)
    {
        if (channel.connection != this || !channel.alive()) return
        map.remove(channel.id)
        if (channelReason == ChannelReason.LOCAL)
        {
            val buf = ByteBufAllocator.DEFAULT.buffer(4)
            buf.writeShort(2)
            buf.writeShort(channel.id.toInt())
            send0(buf)
        }
        connectionHandlers.forEach { runSave { it.onChannelClosed(channel, channelReason) } }
    }

    operator fun get(id: UShort): Channel?
    {
        return map[id]
    }

    //获取最小可用的频道id
    fun getMinAvailableChannelId(start: UShort = 16U): UShort
    {
        var id = start
        while (map.containsKey(id) && map.size < 65536) id++
        if (map.size >= 65536) throw IllegalStateException("no available channel id")
        return id
    }

    fun randomAvailableChannelId(): UShort
    {
        return getMinAvailableChannelId((UShort.MIN_VALUE..UShort.MAX_VALUE).random().toUShort())
    }

    fun getChannelCount(): Int = map.size
    fun getChannelIds(): Set<UShort> = map.keys
    fun getChannels(): Set<Channel> = map.values.toSet()
    val allChannelIds: Set<UShort> by lazy { (0U..65535U).map { it.toUShort() }.toSet() }
    fun getAvailableChannelIds(): Set<UShort> = allChannelIds.minus(getChannelIds())
    fun getAvailableChannelCount(): Int = 65536 - getChannelCount()

    /* ping */

    //最后一次ping的结果
    private var ping: Long = 0

    //发送但未收到pong的ping
    private val pendingPing = mutableSetOf<Long>()

    fun ping(): Long
    {
        //如果没有未收到pong的ping，则返回最后一次ping的结果
        if (pendingPing.isEmpty()) return ping
        //如果有未收到pong的ping，则返回最早的max(最早未收到pong的ping与当前时间差,最后一次ping的结果)
        return max(System.currentTimeMillis() - pendingPing.min(), ping)
    }
}