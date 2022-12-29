package me.lanzhi.api.config;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Map;

/**
 * 继承此接口来进行自动序列化/反序列化
 */
public interface AutoSerialize extends ConfigurationSerializable
{
    /**
     * 注册一个自动序列化/反序列化的类
     *
     * @param clazz 需要注册的类
     */
    public static void registerClass(Class<? extends AutoSerialize> clazz)
    {
        AutoSerializeImpl.registerClass(clazz);
    }

    /**
     * 反序列化一个对象
     *
     * @param map 序列化的数据
     * @return 反序列化后的对象
     */
    public static AutoSerialize deserialize(Map<String,Object> map)
    {
        return AutoSerializeImpl.deserialize(map);
    }

    /**
     * 序列化一个对象
     *
     * @param object 需要序列化的对象
     * @return 序列化后的数据
     */
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
