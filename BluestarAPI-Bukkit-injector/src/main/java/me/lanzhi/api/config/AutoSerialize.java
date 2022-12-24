package me.lanzhi.api.config;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Map;

/**
 * 继承此接口来进行自动序列化/反序列化
 */
public interface AutoSerialize extends ConfigurationSerializable
{
    public static void registerClass(Class<? extends AutoSerialize> clazz)
    {
        AutoSerializeImpl.registerClass(clazz);
    }

    public static AutoSerialize deserialize(Map<String,Object> map)
    {
        return AutoSerializeImpl.deserialize(map);
    }

    public static Map<String,Object> serialize(AutoSerialize object)
    {
        return AutoSerializeImpl.serialize(object);
    }

    /**
     * 序列化,如果你重新了该方法,那所有自动序列化功能都不会生效
     *
     * @return 序列化结果
     */
    @Override
    default Map<String,Object> serialize()
    {
        Class<? extends AutoSerialize> clazz=this.getClass();
        AutoSerializeImpl.registerClass(clazz);
        return AutoSerializeImpl.serialize(this,clazz);
    }
}
