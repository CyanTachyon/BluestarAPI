package me.lanzhi.api.config;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Map;

/**
 * 继承此接口来进行自动序列化/反序列化
 */
public interface AutoSerializeInterface extends ConfigurationSerializable
{
    public static void registerClass(Class<? extends AutoSerializeInterface> clazz)
    {
        AutoSerialize.registerClass(clazz);
    }

    public static AutoSerializeInterface deserialize(Map<String,Object> map)
    {
        return AutoSerialize.deserialize(map);
    }

    public static Map<String,Object> serialize(AutoSerializeInterface object)
    {
        return AutoSerialize.serialize(object);
    }

    /**
     * 序列化,如果你重新了该方法,那所有自动序列化功能都不会生效
     *
     * @return 序列化结果
     */
    @Override
    default Map<String,Object> serialize()
    {
        Class<? extends AutoSerializeInterface> clazz=this.getClass();
        AutoSerialize.registerClass(clazz);
        return AutoSerialize.serialize(this,clazz);
    }
}
