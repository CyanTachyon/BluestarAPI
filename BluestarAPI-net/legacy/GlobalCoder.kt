package me.lanzhi.api

import io.netty.buffer.ByteBuf
import io.netty.handler.codec.MessageToByteEncoder

interface GlobalCoder : GlobalEncoder, GlobalDecoder, Coder<ByteBuf, ByteBuf>
interface GlobalEncoder : Encoder<ByteBuf, ByteBuf>
{
    override fun canEncodeClass(): Class<in ByteBuf> = ByteBuf::class.java
}

interface GlobalDecoder : Decoder<ByteBuf, ByteBuf>
{
    override fun canDecodeClass(): Class<in ByteBuf> = ByteBuf::class.java
}

internal fun GlobalCoder.toEncoder(): MessageToByteEncoder<ByteBuf>
{
    return object : MessageToByteEncoder<ByteBuf>()
    {
        override fun encode(ctx: io.netty.channel.ChannelHandlerContext?, msg: ByteBuf?, out: ByteBuf?)
        {
            if (msg != null) out?.writeBytes(this@toEncoder.encode(msg))
        }
    }
}

internal fun GlobalCoder.toDecoder(): MessageToByteEncoder<ByteBuf>
{
    return object : MessageToByteEncoder<ByteBuf>()
    {
        override fun encode(ctx: io.netty.channel.ChannelHandlerContext?, msg: ByteBuf?, out: ByteBuf?)
        {
            if (msg != null) out?.writeBytes(this@toDecoder.decode(msg))
        }
    }
}