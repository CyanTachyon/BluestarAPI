package me.lanzhi.bluestarapi.Api.config;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
            field.setAccessible(true);
            try
            {
                if (field.isAnnotationPresent(SpecialSerialize.class))
                {
                    SpecialSerialize specialSerialize=field.getAnnotation(SpecialSerialize.class);
                    if (!specialSerialize.doSerialize())
                    {
                        continue;
                    }
                    Method method=clazz.getMethod(specialSerialize.serialize(),field.getType());
                    map.put(field.getName(),method.invoke(this,field.get(this)));
                }
                else
                {
                    map.put(field.getName(),field.get(this));
                }
            }
            catch (Throwable e)
            {
                Bukkit.getLogger().warning(ChatColor.RED+"变量 "+field.getName()+" 序列化失败");
            }
        }
        return map;
    }
}
