package me.nullaqua.api.net

import java.io.*
import java.net.Socket
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.stream.Stream
import me.nullaqua.api.kotlin.lock
import me.nullaqua.api.kotlin.notifyAll
import me.nullaqua.api.kotlin.wait
import java.util.concurrent.LinkedBlockingDeque

/**
 * 一个数据包传输流
 */
abstract class PacketStream<T: PacketStream<T>>(var coderGroup: PacketCoderGroup = PacketCoderGroup())
{
    var onClose: (T.()->Unit) = {}
    abstract fun alive(): Boolean
    abstract fun close()
    abstract fun send(packet: Packet)
    abstract val pipe: PacketPipe

    companion object
    {
        fun create(
            `in`: InputStream,
            `out`: OutputStream,
            coderGroup: PacketCoderGroup = PacketCoderGroup()
        ): DefaultPacketStream = DefaultPacketStream(`in`, `out`, coderGroup)

        fun create(
            socket: Socket,
            `in`: InputStream = socket.getInputStream(),
            `out`: OutputStream = socket.getOutputStream(),
            coderGroup: PacketCoderGroup = PacketCoderGroup()
        ): PacketConnection = PacketConnection(socket, `in`, `out`, coderGroup)
    }
}

/**
 * 默认形式的数据包传输流,由一个输入流和一个输出流组成
 */
open class DefaultPacketStream(
    `in`: InputStream,
    `out`: OutputStream,
    coderGroup: PacketCoderGroup = PacketCoderGroup()
): PacketStream<DefaultPacketStream>(coderGroup)
{
    private val input = DataInputStream(`in`)
    private val output = DataOutputStream(`out`)
    private var isClosed = false
    private fun readNext(): Packet = lock()
    {
        try
        {
            val length = input.readInt()
            val bytes = ByteArray(length)
            input.readFully(bytes)
            return coderGroup.decode(bytes)
        }
        catch (e: Throwable)
        {
            close()
            throw e
        }
    }

    override fun alive() = !isClosed
    override fun close()
    {
        if (!isClosed)
        {
            Thread.currentThread().stackTrace.joinToString("\n").let(::println)
            isClosed = true
            runCatching { onClose() }
            runCatching(input::close)
            runCatching(output::close)
        }
    }

    override fun send(packet: Packet) = coderGroup.encode(packet).let()
    {
        output.writeInt(it.size)
        output.write(it)
    }

    override val pipe: PacketPipe = PacketPipe.create(::readNext)
}

class PacketConnection(
    val socket: Socket,
    `in`: InputStream = socket.getInputStream(),
    `out`: OutputStream = socket.getOutputStream(),
    coderGroup: PacketCoderGroup = PacketCoderGroup()
): DefaultPacketStream(`in`, `out`, coderGroup)
{
    override fun close()
    {
        socket.close()
        super.close()
    }

    override fun alive() = socket.isConnected&&super.alive()
}

abstract class PacketPipe
{
    abstract fun peekOrNull(): Packet?
    abstract fun poll(): Packet?

    @Throws(EOFException::class)
    abstract fun peek(): Packet

    @Throws(EOFException::class)
    abstract fun take(): Packet
    abstract fun close()
    fun skipWhile(predicate: (Packet)->Boolean) = lock()
    {
        while (true)
        {
            val packet =
                runCatching { peek() }.onFailure { if (it is EOFException) return else throw it }.getOrNull() ?: return
            if (!predicate(packet)) return
            take()
        }
    }

    fun skipUntil(predicate: (Packet)->Boolean) = lock()
    {
        while (true)
        {
            val packet =
                runCatching { peek() }.onFailure { if (it is EOFException) return else throw it }.getOrNull() ?: return
            if (predicate(packet)) return
            take()
        }
    }

    fun skip(count: Int) = lock { repeat(count) { take() } }
    fun forEach(action: (Packet)->Unit)
    {
        while (true) lock()
        {
            val packet = runCatching { take() }.getOrNull()?:return
            action(packet)
        }
    }

    companion object
    {
        fun create(generator: ()->Packet) = object: PacketPipe()
        {
            private var packet: Packet? = null
                set(value) = this0.lock()
                {
                    field = value
                    this0.notifyAll()
                }
            private val this0: PacketPipe
                get() = this
            private var gen=generator

            init
            {
                Thread()
                {
                    while (true)
                    {
                        runCatching { gen() }.apply()
                        {
                            println("get: ${this.getOrNull()}")
                            this0.lock()
                            {
                                println("get and wait: ${this.getOrNull()}")
                                while (this0.peekOrNull()!=null) this0.wait()
                                println("set ${this.getOrNull()}")
                                packet = this.getOrNull()
                            }
                        }.onFailure { return@Thread }
                    }
                }.start()
            }

            override fun peekOrNull(): Packet? = this0.lock { packet }
            override fun peek(): Packet = this0.lock { packet ?: run { this0.wait();packet ?: throw EOFException() } }
            override fun poll(): Packet? = this0.lock { peekOrNull().also { packet = null } }
            override fun take(): Packet = this0.lock { peek().also { packet = null } }
            override fun close() { gen = { throw EOFException() } }
        }

        fun create(): Pair<PacketPipe, (Packet)->Unit>
        {
            val queue: BlockingQueue<Packet> = LinkedBlockingQueue()
            val obj = object: PacketPipe()
            {
                private val this0: PacketPipe
                    get() = this
                private var closed = false
                fun waitPacket(): Packet = this0.lock()
                {
                    if (queue.isNotEmpty()) return queue.peek()
                    if (closed) throw EOFException()
                    this0.wait()
                    if (queue.isNotEmpty()) return queue.peek()
                    else
                    {
                        closed = true
                        throw EOFException()
                    }
                }

                override fun peekOrNull(): Packet? = this0.lock { queue.peek() }
                override fun peek(): Packet = this0.lock { waitPacket() }
                override fun poll(): Packet? = this0.lock { queue.poll() }
                override fun take(): Packet = this0.lock { waitPacket().apply { queue.poll() } }
                override fun close() = this0.lock { this0.notifyAll() }
            }
            return obj to { obj.lock { queue.add(it); obj.notifyAll() } }
        }
    }
}