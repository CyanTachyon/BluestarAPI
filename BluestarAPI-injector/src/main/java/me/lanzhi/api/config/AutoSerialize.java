package me.lanzhi.api.config;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

final public class AutoSerialize implements AutoSerializeInterface
{
    public final static String nameOfAutoSerialize;
    static
    {
        nameOfAutoSerialize=JavaPlugin.getProvidingPlugin(AutoSerialize.class).getName()+".AutoSerialize";
        register();
    }

    public static void register()
    {
        ConfigurationSerialization.unregisterClass(AutoSerialize.class);
        ConfigurationSerialization.registerClass(AutoSerialize.class,nameOfAutoSerialize);
    }

    private final static HashMap<String, Class<? extends AutoSerializeInterface>> classNames=new HashMap<>();

    public static AutoSerializeInterface deserialize(Map<String, Object> map)
    {
        String clazzName=(String) map.get("class");
        Class<? extends AutoSerializeInterface> clazz;
        clazz=classNames.get(clazzName);
        if (clazz==null)
        {
            Bukkit.getLogger().warning(ChatColor.RED+"反序列化失败,未找到类: \""+clazzName+"\"");
            return null;
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

    public static Map<String, Object> serialize(Object object)
    {
        return serialize(object,object.getClass());
    }

    public static Map<String, Object> serialize(Object object,Class<?> clazz)
    {
        HashMap<String, Object> map=new HashMap<>();
        String clazzName=getClassName(clazz);
        //System.out.println("序列化: "+clazzName);
        map.put("class",clazzName);
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
                    if (specialSerialize.serialize().isEmpty())
                    {
                        continue;
                    }
                    Method method=clazz.getMethod(specialSerialize.serialize(),field.getType());
                    map.put(field.getName(),method.invoke(object,field.get(object)));
                }
                else
                {
                    map.put(field.getName(),field.get(object));
                }
            }
            catch (Throwable e)
            {
                Bukkit.getLogger().warning(ChatColor.RED+"变量 "+field.getName()+" 序列化失败");
            }
        }
        return map;
    }

    @Override
    public Map<String, Object> serialize()
    {
        return new HashMap<>();
    }

    public static void registerClass(Class<? extends AutoSerializeInterface> clazz)
    {
        classNames.put(getClassName(clazz),clazz);
    }
    private static String getClassName(Class<?> clazz)
    {
        if (clazz.isAnnotationPresent(SerializeAs.class))
        {
            return clazz.getAnnotation(SerializeAs.class).value();
        }
        return clazz.getName();
    }
}
