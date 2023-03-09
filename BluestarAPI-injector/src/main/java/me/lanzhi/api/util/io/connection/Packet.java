package me.lanzhi.api.util.io.connection;

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
     *
     * @return 字节数组
     */
    public abstract byte[] toByteArray();
}