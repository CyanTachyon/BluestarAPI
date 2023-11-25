package me.nullaqua.api.io.connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 一个数据包类型
 *
 * @see Packet 一个数据包
 */
public abstract class PacketType
{
    /**
     * 数据包类型的ID
     */
    private final byte id;

    /**
     * 通过ID来创建一个数据包类型
     *
     * @param id ID
     */
    public PacketType(byte id)
    {
        this.id=id;
    }

    /**
     * 获取数据包类型的ID
     *
     * @return ID
     */
    public byte id()
    {
        return id;
    }

    /**
     * 将字节数组转换为数据包,是{@link Packet#write(DataOutputStream)} }的反向操作<br>
     * 即:
     * <pre>
     *      Packet packet;
     *      Packet packet0=packet.type().fromByteArray(packet.toByteArray());
     *      assert packet.equals(packet0);</pre>
     * 应当成立
     *
     * @return 数据包
     */
    public abstract Packet read(DataInputStream in) throws IOException;

    @Override
    public String toString()
    {
        return "PacketType{id="+id+",name="+getClass().getSimpleName()+"}";
    }

    public static final class UnknownPacketType extends PacketType
    {
        private final byte id;

        public UnknownPacketType(byte id)
        {
            super(id);
            this.id=id;
        }

        @Override
        public Packet read(DataInputStream data) throws IOException
        {
            return new Packet.UnknownPacket(id);
        }
    }
}