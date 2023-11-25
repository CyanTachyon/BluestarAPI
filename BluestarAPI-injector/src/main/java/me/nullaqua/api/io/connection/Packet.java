package me.nullaqua.api.io.connection;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 一个数据包
 *
 * @see PacketConnection 一个数据包连接
 */
public abstract class Packet
{
    /**
     * 数据包类型
     */
    private final PacketType type;

    /**
     * 通过数据包类型来创建一个数据包
     *
     * @param type 数据包类型
     */
    public Packet(PacketType type)
    {
        this.type=type;
    }

    /**
     * 获取数据包类型
     *
     * @return 数据包类型
     */
    public PacketType type()
    {
        return type;
    }

    /**
     * 将数据包转换为字节数组用于传输
     */
    public abstract void write(DataOutputStream out) throws IOException;

    public static final class UnknownPacket extends Packet
    {
        public UnknownPacket(byte type)
        {
            super(new PacketType.UnknownPacketType(type));
        }

        @Override
        public void write(DataOutputStream out)
        {
        }
    }
}