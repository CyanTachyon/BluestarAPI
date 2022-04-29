package me.lanzhi.bluestarapi.Api;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public abstract class AutoConfigSerialize implements ConfigurationSerializable
{
    @Override
    public Map<String, Object> serialize()
    {
        HashMap<String,Object> map=new HashMap<>();
        Class<? extends AutoConfigSerialize> clazz=this.getClass();
        Field[] fields=clazz.getDeclaredFields();
        for (Field field:fields)
        {
            if ((field.getModifiers()&8)!=0)
            {
                continue;
            }
            try
            {
                field.setAccessible(true);
                map.put(field.getName(),field.get(this));
            }
            catch (Throwable e)
            {}
        }
        return map;
    }
    public static AutoConfigSerialize deserialize(Map<String,Object>map)
    {
        Class<? extends AutoConfigSerialize> clazz;
        try
        {
            clazz=(Class<? extends AutoConfigSerialize>)Class.forName((String) map.get("=="));
        }
        catch (Throwable e)
        {
            return null;
        }
        AutoConfigSerialize object;
        try
        {
            object=clazz.cast(clazz.getMethod("getNewObject").invoke(null));
        }
        catch (Throwable e)
        {
            return null;
        }
        Field[] fields=clazz.getDeclaredFields();
        for (Field field:fields)
        {
            if ((field.getModifiers()&8)!=0)
            {
                continue;
            }
            field.setAccessible(true);
            try
            {
                field.set(object,map.get(field.getName()));
            }
            catch (IllegalAccessException e)
            {}
        }
        return object;
    }
    public abstract AutoConfigSerialize getNewObject();
}
