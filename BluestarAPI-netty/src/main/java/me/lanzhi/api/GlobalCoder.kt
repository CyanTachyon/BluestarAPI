package me.lanzhi.api

import io.netty.buffer.ByteBuf
import io.netty.handler.codec.MessageToByteEncoder

open class GlobalCoder
{
    @Throws(Exception::class)
    open fun decoder(byteBuf: ByteBuf): ByteBuf = byteBuf

    @Throws(Exception::class)
    open fun encoder(byteBuf: ByteBuf): ByteBuf = byteBuf

    internal fun toEncoder(): MessageToByteEncoder<ByteBuf>
    {
        return object : MessageToByteEncoder<ByteBuf>()
        {
            override fun encode(ctx: io.netty.channel.ChannelHandlerContext?, msg: ByteBuf?, out: ByteBuf?)
            {
                if (msg != null) out?.writeBytes(encoder(msg))
            }
        }
    }

    internal fun toDecoder(): MessageToByteEncoder<ByteBuf>
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