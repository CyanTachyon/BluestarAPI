package me.lanzhi.api.util.io.connection;

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
     * 将字节数组转换为数据包,是{@link Packet#toByteArray()}的反向操作
     * 即: {@code
     * Packet packet;
     * Packet packet0=packet.type().fromByteArray(packet.toByteArray());
     * assert packet.equals(packet0);
     * } 应该成立
     *
     * @param data 字节数组
     * @return 数据包
     */
    public abstract Packet fromByteArray(byte[] data);

    @Override
    public String toString()
    {
        return "PacketType{id="+id+",name="+getClass().getSimpleName()+"}";
    }
}