package me.nullaqua.api.net

import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

fun main()
{
    Thread(::server).start()
    Thread(::client).start()
}


fun server()
{
    val socket = Socket()
    socket.bind(InetSocketAddress("localhost", 2333))
    socket.connect(InetSocketAddress("localhost", 2334))
    DataInputStream(socket.getInputStream()).readUTF().apply(::println)
    socket.close()
}

fun client()
{
    val socket = Socket()
    socket.bind(InetSocketAddress("localhost", 2334))
    socket.connect(InetSocketAddress("localhost", 2333))
    DataOutputStream(socket.getOutputStream()).writeUTF("HELLO WORLD!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
    socket.close()
}



data class TestPacket(val string: String): Packet()
{
    override fun toString(): String = "TestPacket(string='$string')"
}
object TestPacketCoder: PacketCoder<TestPacket>(0U,TestPacket::class.java)
{
    override fun encode(packet: TestPacket, out: DataOutputStream)
    {
        out.writeUTF(packet.string)
    }
    override fun decode(input: DataInputStream): TestPacket
    {
        return TestPacket(input.readUTF())
    }
}