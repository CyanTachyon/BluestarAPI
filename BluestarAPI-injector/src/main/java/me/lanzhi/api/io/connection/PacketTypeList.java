package me.lanzhi.api.io.connection;

/**
 * 一个数据包类型列表,由若干个数据包类型组成
 */
public final class PacketTypeList implements Cloneable
{
    private final PacketType[] types=new PacketType[256];

    /**
     * 添加一个数据包类型
     *
     * @param type 数据包类型
     */
    public void add(PacketType type)
    {
        types[type.id()&0xff]=type;
    }

    /**
     * 获取一个数据包类型
     *
     * @param id 数据包类型的ID
     * @return 数据包类型
     */
    public PacketType get(byte id)
    {
        return types[id&0xff];
    }

    /**
     * 获取一个数据包类型
     *
     * @param id 数据包类型的ID
     * @return 数据包类型
     */
    public PacketType get(int id)
    {
        return types[id&0xff];
    }

    @Override
    public PacketTypeList clone()
    {
        PacketTypeList list=new PacketTypeList();
        System.arraycopy(types,0,list.types,0,types.length);
        return list;
    }

    @Override
    public String toString()
    {
        StringBuilder builder=new StringBuilder();
        builder.append("PacketTypeList{");
        for (int i=0;i<types.length;i++)
        {
            if (types[i]!=null)
            {
                builder.append(i).append(":").append(types[i]).append(",");
            }
        }
        builder.append("}");
        return builder.toString();
    }
}