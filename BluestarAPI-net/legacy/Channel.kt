package me.lanzhi.api

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator

sealed class Channel
{
    abstract val id: UShort
    abstract val connection: Connection

    val handlers: MutableList<ChannelHandler> = mutableListOf()
    private val encoders: MutableList<Encoder<*, *>> = mutableListOf()
    private val decoders: MutableList<Decoder<*, *>> = mutableListOf()

    fun addCoderFirst(coder: Coder<*, *>)
    {
        addEncoderLast(coder)
        addDecoderFirst(coder)
    }

    fun addCoderLast(coder: Coder<*, *>)
    {
        addEncoderFirst(coder)
        addDecoderLast(coder)
    }

    fun addEncoderFirst(encoder: Encoder<*, *>)
    {
        encoders.add(0, encoder)
    }

    fun addEncoderLast(encoder: Encoder<*, *>)
    {
        encoders.add(encoder)
    }

    fun addDecoderFirst(decoder: Decoder<*, *>)
    {
        decoders.add(0, decoder)
    }

    fun addDecoderLast(decoder: Decoder<*, *>)
    {
        decoders.add(decoder)
    }

    fun removeCoder(coder: Coder<*, *>)
    {
        removeEncoder(coder)
        removeDecoder(coder)
    }

    fun removeEncoder(encoder: Encoder<*, *>)
    {
        encoders.remove(encoder)
    }

    fun removeDecoder(decoder: Decoder<*, *>)
    {
        decoders.remove(decoder)
    }

    abstract fun send(data: Any)

    abstract fun close()

    abstract fun alive(): Boolean

    internal fun onMessage(buf: ByteBuf)
    {
        logger.config("received message from channel $id")

        var x: Any = buf

        decoders.forEach {
            try
            {
                if (it.canDecodeClass().isInstance(x))
                {
                    logger.config("decoding message from channel $id with decoder ${it.javaClass.name}")
                    x = (it as Decoder<Any, Any>).decode(x)
                }
                else
                {
                    logger.config("decoder ${it.javaClass.name} cannot decode message from channel $id")
                }
            }
            catch (e: Exception)
            {
                logger.warning("decoder ${it.javaClass.name} failed to decode message from channel $id", e)
            }
        }

        handlers.forEach {
            try
            {
                logger.config("handling message from channel $id with handler ${it.javaClass.name}")
                it.onReceive(this, x)
            }
            catch (e: Exception)
            {
                logger.warning("handler ${it.javaClass.name} failed to handle message from channel $id", e)
            }
        }
    }

    internal fun onSend(o: Any): ByteBuf
    {
        logger.config("sending message to channel $id")
        var x: Any = o
        encoders.forEach {
            try
            {
                if (it.canEncodeClass().isInstance(x))
                {
                    logger.config("encoding message to channel $id with encoder ${it.javaClass.name}")
                    x = (it as Encoder<Any, Any>).encode(x)
                }
                else
                {
                    logger.config("encoder ${it.javaClass.name} cannot encode message to channel $id")
                }
            }
            catch (e: Exception)
            {
                logger.warning("encoder ${it.javaClass.name} failed to encode message to channel $id", e)
            }
        }
        return when (x)
        {
            is ByteBuf -> x as ByteBuf
            is ByteArray -> ByteBufAllocator.DEFAULT.buffer().writeBytes(x as ByteArray)
            is String ->
            {
                val buf = ByteBufAllocator.DEFAULT.buffer((x as String).length)
                buf.writeCharSequence(x as String, Charsets.UTF_8)
                buf
            }

            else -> throw IllegalArgumentException("unsupported data type")
        }
    }
}

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

interface ChannelHandler
{
    fun onReceive(channel: Channel, data: Any)
}

class TransformChannelHandler(private val otherChannel: Channel) : ChannelHandler
{
    override fun onReceive(channel: Channel, data: Any)
    {
        otherChannel.send(data)
    }
}

infix fun Channel.transformTo(otherChannel: Channel)
{
    handlers.clear()
    handlers.add(TransformChannelHandler(otherChannel))
}