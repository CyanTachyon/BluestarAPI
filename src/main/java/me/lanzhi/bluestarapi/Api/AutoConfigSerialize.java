package me.lanzhi.bluestarapi.Api;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public interface AutoConfigSerialize extends ConfigurationSerializable
{
    @Override
    public default Map<String, Object> serialize()
    {
        HashMap<String,Object> map=new HashMap<>();
        Class<? extends AutoConfigSerialize> clazz=this.getClass();
        map.put("Class",clazz.getName());
        map.put("==",AutoConfigSerialize.class.getName());
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
        this.getClass();
        return map;
    }
    public static AutoConfigSerialize deserialize(Map<String,Object> map)
    {
        System.out.println("反序列化开始");
        Class<? extends AutoConfigSerialize> clazz;
        try
        {
            clazz=(Class<? extends AutoConfigSerialize>)Class.forName((String) map.get("Class"));
        }
        catch (Throwable e)
        {
            System.out.println("获取class失败");
            return null;
        }
        System.out.println("找到class: "+clazz.getName());
        AutoConfigSerialize object;
        try
        {
            object=clazz.cast(clazz.newInstance());
        }
        catch (Throwable e)
        {
            System.out.println("获取对象失败");
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
            {
                System.out.println("设置变量失败");
            }
        }
        return object;
    }
}
