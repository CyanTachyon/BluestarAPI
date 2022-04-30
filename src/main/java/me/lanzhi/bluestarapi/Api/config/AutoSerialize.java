package me.lanzhi.bluestarapi.Api.config;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class AutoSerialize implements AutoSerializeInterface
{
    public final static String nameOfAutoSerialize="BluestarAPI.AutoSerialize";
    private final static HashMap<String,Class<? extends AutoSerializeInterface>> classNames=new HashMap<>();
    private final static HashMap<Class<? extends AutoSerializeInterface>,String> allClazz=new HashMap<>();

    public static AutoSerializeInterface deserialize(Map<String, Object> map)
    {
        String clazzName=(String) map.get("class");
        System.out.println("反序列化: "+clazzName);
        Class<? extends AutoSerializeInterface> clazz;
        clazz=AutoSerialize.classNames.get(clazzName);
        if (clazz==null)
        {
            Bukkit.getLogger().warning(ChatColor.RED+"反序列化失败,未找到类: \""+clazzName+"\"");
        }
        AutoSerializeInterface object;
        try
        {
            object=clazz.newInstance();
        }
        catch (Throwable e)
        {
            System.out.println(ChatColor.RED+"新建对象失败");
            throw new RuntimeException(e);
        }
        Field[] fields=clazz.getDeclaredFields();
        for (Field field: fields)
        {
            field.setAccessible(true);
            try
            {
                if (field.isAnnotationPresent(SpecialSerialize.class))
                {
                    SpecialSerialize specialSerialize=field.getAnnotation(SpecialSerialize.class);
                    if (specialSerialize.deserialize().isEmpty())
                    {
                        continue;
                    }
                    Method serializeMethod=clazz.getMethod(specialSerialize.serialize(),field.getType());
                    Method deserializeMethod=clazz.getMethod(specialSerialize.deserialize(),serializeMethod.getReturnType());
                    field.set(object,deserializeMethod.invoke(object,map.get(field.getName())));
                }
                else
                {
                    field.set(object,map.get(field.getName()));
                }
            }
            catch (Throwable e)
            {
                Bukkit.getLogger().warning(ChatColor.RED+"变量 "+field.getName()+" 反序列化失败");
            }
        }
        return object;
    }
    public static void registerClass(Class<? extends AutoSerializeInterface> clazz)
    {
        registerClass(clazz,clazz.getName());
    }
    public static void registerClass(Class<? extends AutoSerializeInterface> clazz,String name)
    {
        classNames.put(name,clazz);
        allClazz.put(clazz,name);
    }
    public static void unregisterClass(Class<? extends AutoSerializeInterface>clazz)
    {
        classNames.values().remove(clazz);
        allClazz.remove(clazz);
    }
    public static void unregisterClass(String name)
    {
        classNames.remove(name);
        allClazz.values().remove(name);
    }
    protected static String getClassName(Class<? extends AutoSerializeInterface>clazz)
    {
        return allClazz.get(clazz);
    }
}
