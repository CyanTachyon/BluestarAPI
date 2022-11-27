package me.lanzhi.api.reflect;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import sun.misc.Unsafe;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.*;

public final class Accessor
{
    static final MethodHandles.Lookup LOOKUP;
    static final MethodType STATIC_FIELD_GETTER=MethodType.methodType(Object.class);
    static final MethodType STATIC_FIELD_SETTER=MethodType.methodType(Void.TYPE,Object.class);
    static final MethodType VIRTUAL_FIELD_GETTER=MethodType.methodType(Object.class,Object.class);
    static final MethodType VIRTUAL_FIELD_SETTER=MethodType.methodType(Void.TYPE,Object.class,Object.class);

    static
    {
        MethodHandles.Lookup lookup;
        try
        {
            Class<Unsafe> unsafeClass=Unsafe.class;
            Field theUnsafe=unsafeClass.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Unsafe unsafe=(Unsafe) theUnsafe.get(null);
            Field trustedLookup=MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            long offset=unsafe.staticFieldOffset(trustedLookup);
            Object baseValue=unsafe.staticFieldBase(trustedLookup);
            lookup=(MethodHandles.Lookup) unsafe.getObject(baseValue,offset);
        }
        catch (Exception e)
        {
            lookup=MethodHandles.lookup();
        }
        LOOKUP=lookup;
    }

    public static Class<?> getClass(String name)
    {
        try
        {
            return Class.forName(name);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static Plugin getCallerPlugin(int x)
    {
        return JavaPlugin.getProvidingPlugin(getCallerClass(x+1));
    }

    public static Plugin getCallerPlugin()
    {
        return getCallerPlugin(1);
    }

    public static Class<?> getCallerClass(int x)
    {
        return getClass(getCaller(x+1).getClassName());
    }

    public static Class<?> getCallerClass()
    {
        return getCallerClass(1);
    }

    public static StackTraceElement getCaller()
    {
        return getCaller(1);
    }

    public static StackTraceElement getCaller(int x)
    {
        return Thread.currentThread().getStackTrace()[x+2];
    }

    public static boolean isVisibility(Class<?> clazz)
    {
        return !clazz.getPackage().equals(Accessor.class.getPackage());
    }

    public static <T> T newInstance(T o)
    {
        Class<?> type=o.getClass();
        try
        {
            return (T) ConstructorAccessor.getConstructor(type).invoke();
        }
        catch (Throwable e)
        {
        }
        try
        {
            return (T) type.newInstance();
        }
        catch (Throwable e)
        {
        }

        Object oo=null;
        try
        {
            oo=MethodAccessor.getDeclaredMethod(type,"clone").invoke(o);
        }
        catch (Throwable e)
        {
        }
        if (oo!=null&&oo.getClass()==o.getClass())
        {
            return (T) oo;
        }


        for (Constructor<?> constructor: type.getDeclaredConstructors())
        {
            try
            {
                return (T) new ConstructorAccessor<>(constructor).invoke(new Object[constructor.getExceptionTypes().length]);
            }
            catch (Throwable e)
            {
            }
        }
        for (Method method: type.getDeclaredMethods())
        {
            if (Modifier.isStatic(method.getModifiers())&&
                method.getReturnType()==type&&
                method.getParameterTypes().length==0)
            {
                try
                {
                    return (T) new MethodAccessor(method).invoke(null);
                }
                catch (Throwable e)
                {
                }
            }
        }
        for (Method method: type.getDeclaredMethods())
        {
            if (Modifier.isStatic(method.getModifiers())&&method.getReturnType()==type)
            {
                try
                {
                    return (T) new MethodAccessor(method).invoke(null,new Object[method.getParameterTypes().length]);
                }
                catch (Throwable e)
                {
                }
            }
        }
        return null;
    }

    public static <T> T newInstance(Class<T> type)
    {
        try
        {
            return ConstructorAccessor.getConstructor(type).invoke();
        }
        catch (Throwable e)
        {
        }

        try
        {
            return type.newInstance();
        }
        catch (Throwable e)
        {
        }
        for (Constructor<?> constructor: type.getDeclaredConstructors())
        {
            try
            {
                return (T) new ConstructorAccessor<>(constructor).invoke(new Object[constructor.getExceptionTypes().length]);
            }
            catch (Throwable e)
            {
            }
        }

        for (Method method: type.getDeclaredMethods())
        {
            if (Modifier.isStatic(method.getModifiers())&&
                method.getReturnType()==type&&
                method.getParameterTypes().length==0)
            {
                try
                {
                    return (T) new MethodAccessor(method).invoke(null);
                }
                catch (Throwable e)
                {
                }
            }
        }
        for (Method method: type.getDeclaredMethods())
        {
            if (Modifier.isStatic(method.getModifiers())&&method.getReturnType()==type)
            {
                try
                {
                    return (T) new MethodAccessor(method).invoke(null,new Object[method.getParameterTypes().length]);
                }
                catch (Throwable e)
                {
                }
            }
        }
        return null;
    }

    public static List<Class<?>> getAllSuperClass(Class<?> type)
    {
        List<Class<?>> clazz=new ArrayList<>();
        if (type==null)
        {
            return clazz;
        }
        while (type!=null)
        {
            clazz.add(type);
            type=type.getSuperclass();
        }
        return clazz;
    }

    public static Class<?> getBothSuperClass(Class<?> a,Class<?> b)
    {
        if (a==null||b==null)
        {
            return Object.class;
        }
        List<Class<?>> aSuperClass=getAllSuperClass(a);
        List<Class<?>> bSuperClass=getAllSuperClass(b);
        for (Class<?> c: aSuperClass)
        {
            if (bSuperClass.contains(c))
            {
                return c;
            }
        }
        return Object.class;
    }

    public static List<FieldAccessor> getFields(Class<?> c)
    {
        List<FieldAccessor> accessors=new ArrayList<>();
        Arrays.asList(c.getDeclaredFields()).forEach(field->accessors.add(new FieldAccessor(field)));
        return accessors;
    }

    private static List<FieldAccessor> getBothFields(Object a,Object b)
    {
        if (a==null||b==null)
        {
            return new ArrayList<>();
        }
        return getBothFields(a.getClass(),b.getClass());
    }

    private static List<FieldAccessor> getBothFields(Class<?> a,Class<?> b)
    {
        return getAllFields(getBothSuperClass(a,b));
    }

    public static List<FieldAccessor> getAllFields(Object o)
    {
        if (o==null)
        {
            return new ArrayList<>();
        }
        return getAllFields(o.getClass());
    }

    public static List<FieldAccessor> getAllFields(Class<?> type)
    {
        List<FieldAccessor> fields=new ArrayList<>();
        for (Class<?> c: getAllSuperClass(type))
        {
            fields.addAll(getFields(c));
        }
        return fields;

    }

    public static boolean isSimpleObject(Object o)
    {
        Class<?> type=o.getClass();
        return type.isPrimitive()||
               type.equals(String.class)||
               type.equals(Long.class)||
               type.equals(Boolean.class)||
               type.equals(Short.class)||
               type.equals(Integer.class)||
               type.equals(Character.class)||
               type.equals(Float.class)||
               type.equals(Double.class)||
               type.equals(Byte.class);
    }

    public static <T extends Serializable> T cloneObject(T o)
    {
        try
        {
            ByteArrayOutputStream bo=new ByteArrayOutputStream();
            ObjectOutputStream oo=new ObjectOutputStream(bo);
            oo.writeObject(o);

            ByteArrayInputStream bi=new ByteArrayInputStream(bo.toByteArray());
            ObjectInputStream oi=new ObjectInputStream(bi);
            return (T) oi.readObject();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static Object cloneObject(Object o)
    {
        if (null==o)
        {
            return null;
        }
        Map<Object,Object> map=new HashMap<>();
        return cloneObject(o,map);
    }

    private static Object cloneObject(Object o,Map<Object,Object> map)
    {
        if (o==null)
        {
            return null;
        }
        if (isSimpleObject(o))
        {
            return o;
        }
        Object newInstance=null;
        newInstance=map.get(o);
        if (newInstance!=null)
        {
            return newInstance;
        }
        if (o.getClass().isArray())
        {
            return cloneArray(o,map);
        }

        if (o instanceof Serializable)
        {
            newInstance=cloneObject((Serializable) o);
            map.put(o,newInstance);
        }
        else
        {
            Class<?> type=o.getClass();
            newInstance=newInstance(o);
            map.put(o,newInstance);
            cloneFields(o,newInstance,map);
        }
        return newInstance;
    }

    private static Object cloneArray(Object o,Map<Object,Object> map)
    {
        if (null==o)
        {
            return null;
        }
        if (!o.getClass().isArray())
        {
            return cloneObject(o,map);
        }
        int len=Array.getLength(o);
        Object array=Array.newInstance(o.getClass().getComponentType(),len);
        map.put(o,array);
        for (int i=0;i<len;i++)
        {
            Array.set(array,i,cloneObject(Array.get(o,i),map));
        }
        return array;
    }

    private static void cloneFields(Object object,Object newObject,Map<Object,Object> map)
    {
        if (object==null||newObject==null)
        {
            return;
        }
        List<FieldAccessor> fields=getBothFields(object,newObject);
        for (FieldAccessor f: fields)
        {
            if (!Modifier.isStatic(f.getField().getModifiers()))
            {
                try
                {
                    f.set(newObject,cloneObject(f.get(object),map));
                }
                catch (Throwable e)
                {
                }
            }
        }
    }

    public static <T> T objectCopy(T a,T b)
    {
        if (Serializable.class.isAssignableFrom(getBothSuperClass(a.getClass(),b.getClass())))
        {
            return (T) cloneObject((Serializable) b);
        }
        Map<Object,Object> map=new HashMap<>();
        map.put(a,b);
        cloneFields(a,b,map);
        return a;
    }
}
