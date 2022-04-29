package me.lanzhi.bluestarapi.Api.config;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public interface AutoSerializeInterface extends ConfigurationSerializable
{
    @Override
    public default Map<String, Object> serialize()
    {
        HashMap<String, Object> map=new HashMap<>();
        Class<? extends AutoSerializeInterface> clazz=this.getClass();
        map.put("class",AutoSerialize.getClassName(clazz));
        map.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY,AutoSerialize.nameOfAutoSerialize);
        Field[] fields=clazz.getDeclaredFields();
        for (Field field: fields)
        {
            if (field.isAnnotationPresent(DoNotSerialize.class))
            {
                continue;
            }
            try
            {
                field.setAccessible(true);
                map.put(field.getName(),field.get(this));
            }
            catch (Throwable e)
            {
            }
        }
        return map;
    }
}
