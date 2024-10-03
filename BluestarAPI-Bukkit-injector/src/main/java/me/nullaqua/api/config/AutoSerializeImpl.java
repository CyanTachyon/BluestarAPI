package me.nullaqua.api.config;

import me.nullaqua.api.reflect.UnsafeOperation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 自动序列化的实现
 */
public final class AutoSerializeImpl implements AutoSerialize
{
    public final static String nameOfAutoSerialize;
    private final static HashMap<String, Class<? extends AutoSerialize>> classNames = new HashMap<>();

    static
    {
        nameOfAutoSerialize = JavaPlugin.getProvidingPlugin(AutoSerializeImpl.class).getName()+".AutoSerialize";
        register();
    }

    private AutoSerializeImpl()
    {
    }

    public static void register()
    {
        ConfigurationSerialization.unregisterClass(AutoSerializeImpl.class);
        ConfigurationSerialization.registerClass(AutoSerializeImpl.class, nameOfAutoSerialize);
    }

    public static AutoSerialize deserialize(Map<String, Object> map)
    {
        String clazzName = (String) map.get("class");
        Class<? extends AutoSerialize> clazz;
        clazz = classNames.get(clazzName);
        if (clazz == null)
        {
            Bukkit.getLogger().warning(ChatColor.RED+"反序列化失败,未找到类: \""+clazzName+"\"");
            return null;
        }
        AutoSerialize object;
        try
        {
            object = UnsafeOperation.blankInstance(clazz);
        }
        catch (Throwable e)
        {
            System.out.println(ChatColor.RED+"新建对象失败");
            throw new RuntimeException(e);
        }
        Field[] fields = clazz.getDeclaredFields();
        for (Field field: fields)
        {
            field.setAccessible(true);
            try
            {
                if (field.isAnnotationPresent(SpecialSerialize.class))
                {
                    SpecialSerialize specialSerialize = field.getDeclaredAnnotation(SpecialSerialize.class);
                    if (specialSerialize.deserialize().isEmpty())
                    {
                        continue;
                    }
                    Method serializeMethod = clazz.getMethod(specialSerialize.serialize(), field.getType());
                    Method deserializeMethod = clazz.getMethod(specialSerialize.deserialize(),
                                                               serializeMethod.getReturnType()
                    );
                    field.set(object, deserializeMethod.invoke(object, map.get(field.getName())));
                }
                else
                {
                    field.set(object, map.get(field.getName()));
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
        return serialize(object, object.getClass());
    }

    public static Map<String, Object> serialize(Object object, Class<?> clazz)
    {
        HashMap<String, Object> map = new HashMap<>();
        String clazzName = getClassName(clazz);
        //System.out.println("序列化: "+clazzName);
        map.put("class", clazzName);
        map.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, AutoSerializeImpl.nameOfAutoSerialize);
        Field[] fields = clazz.getDeclaredFields();
        for (Field field: fields)
        {
            field.setAccessible(true);
            try
            {
                if (field.isAnnotationPresent(SpecialSerialize.class))
                {
                    SpecialSerialize specialSerialize = field.getDeclaredAnnotation(SpecialSerialize.class);
                    if (specialSerialize.serialize().isEmpty())
                    {
                        continue;
                    }
                    Method method = clazz.getMethod(specialSerialize.serialize(), field.getType());
                    map.put(field.getName(), method.invoke(object, field.get(object)));
                }
                else
                {
                    map.put(field.getName(), field.get(object));
                }
            }
            catch (Throwable e)
            {
                Bukkit.getLogger().warning(ChatColor.RED+"变量 "+field.getName()+" 序列化失败");
            }
        }
        return map;
    }

    private static String getClassName(Class<?> clazz)
    {
        if (clazz.isAnnotationPresent(SerializeAs.class))
        {
            return clazz.getDeclaredAnnotation(SerializeAs.class).value();
        }
        return clazz.getName();
    }

    public static void registerClass(Class<? extends AutoSerialize> clazz)
    {
        classNames.put(getClassName(clazz), clazz);
    }

    @Override
    public Map<String, Object> serialize()
    {
        return new HashMap<>();
    }
}
