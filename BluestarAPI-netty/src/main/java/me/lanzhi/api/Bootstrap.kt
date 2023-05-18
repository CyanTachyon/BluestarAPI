package me.lanzhi.api

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelInitializer
import io.netty.handler.codec.MessageToByteEncoder
import io.netty.util.concurrent.Future
import io.netty.util.concurrent.GenericFutureListener
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.concurrent.TimeUnit

/**
 * 多频道连接,可以在一个连接中创建多个频道,相互独立
 */
class ConnectBootstrap : Bootstrap()
{
    init
    {
        super.handler(object : ChannelInitializer<Channel>()
                      {
                          override fun initChannel(ch: Channel?)
                          {
                              ch?.pipeline()?.addFirst(*globalHandler.toTypedArray())
                              globalCoders.forEach { ch?.pipeline()?.addFirst(it.toEncoder(), it.toDecoder()) }
                          }
                      })
    }

    val globalCoders = mutableListOf<GlobalCoder>()
    val globalHandler = mutableListOf<ChannelHandler>()
    val connectInitializer = mutableListOf<ConnectionInitializer>()

    //禁用从父类继承的handler方法
    override fun handler(handler: ChannelHandler?): Bootstrap
    {
        throw UnsupportedOperationException()
    }

    override fun connect(inetHost: String?, inetPort: Int): ConnectFuture = connect(InetAddress.getByName(inetHost), inetPort)

    override fun connect(inetHost: InetAddress?, inetPort: Int): ConnectFuture = connect(InetSocketAddress(inetHost, inetPort))

    override fun connect(remoteAddress: SocketAddress?): ConnectFuture = connect(remoteAddress, null)

    override fun connect(remoteAddress: SocketAddress?, localAddress: SocketAddress?): ConnectFuture
    {
        val future=ConnectFuture(super.connect(remoteAddress, localAddress))
        future.addListener { _ ->
            if (future.isSuccess)
            {
                val connection = future.connection
                if (connection != null) connectInitializer.forEach { it.initChannel(connection) }
            }
        }
        return future
    }
}

open class GlobalCoder
{
    @Throws(Exception::class)
    open fun decoder(byteBuf: ByteBuf): ByteBuf = byteBuf

    @Throws(Exception::class)
    open fun encoder(byteBuf: ByteBuf): ByteBuf = byteBuf

    fun toEncoder(): MessageToByteEncoder<ByteBuf>
    {
        return object : MessageToByteEncoder<ByteBuf>()
        {
            override fun encode(ctx: io.netty.channel.ChannelHandlerContext?, msg: ByteBuf?, out: ByteBuf?)
            {
                if (msg != null) out?.writeBytes(encoder(msg))
            }
        }
    }

    fun toDecoder(): MessageToByteEncoder<ByteBuf>
    {
        return object : MessageToByteEncoder<ByteBuf>()
        {
            override fun encode(ctx: io.netty.channel.ChannelHandlerContext?, msg: ByteBuf?, out: ByteBuf?)
            {
                if (msg != null) out?.writeBytes(decoder(msg))
            }
        }
    }
}

interface ConnectionInitializer
{
    fun initChannel(ch: Connection){}
}

class ConnectFuture(private val channelFuture: ChannelFuture) : ChannelFuture by channelFuture
{
    override fun addListener(listener: GenericFutureListener<out Future<in Void>>?): ConnectFuture
    {
        channelFuture.addListener(listener)
        return this
    }

    override fun addListeners(vararg listeners: GenericFutureListener<out Future<in Void>>?): ConnectFuture
    {
        channelFuture.addListeners(*listeners)
        return this
    }

    override fun removeListener(listener: GenericFutureListener<out Future<in Void>>?): ConnectFuture
    {
        channelFuture.removeListener(listener)
        return this
    }

    override fun removeListeners(vararg listeners: GenericFutureListener<out Future<in Void>>?): ConnectFuture
    {
        channelFuture.removeListeners(*listeners)
        return this
    }

    override fun sync(): ConnectFuture
    {
        channelFuture.sync()
        return this
    }

    override fun syncUninterruptibly(): ConnectFuture
    {
        channelFuture.syncUninterruptibly()
        return this
    }

    override fun await(): ConnectFuture
    {
        channelFuture.await()
        return this
    }

    override fun awaitUninterruptibly(): ConnectFuture
    {
        channelFuture.awaitUninterruptibly()
        return this
    }

    var connection: Connection? = null
        private set
        get()
        {
            if (field != null) return field
            val channel = channelFuture.channel()
            if (channel != null)
            {
                field = Connection(channel)
            }
            return field
        }
}