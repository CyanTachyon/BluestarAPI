package me.nullaqua.api.reflect;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.*;

public final class ReflectAccessor
{
    static final MethodHandles.Lookup LOOKUP;
    static final MethodType STATIC_FIELD_GETTER=MethodType.methodType(Object.class);
    static final MethodType STATIC_FIELD_SETTER=MethodType.methodType(Void.TYPE,Object.class);
    static final MethodType VIRTUAL_FIELD_GETTER=MethodType.methodType(Object.class,Object.class);
    static final MethodType VIRTUAL_FIELD_SETTER=MethodType.methodType(Void.TYPE,Object.class,Object.class);

    private ReflectAccessor()
    {
    }

    static
    {
        MethodHandles.Lookup lookup;
        try
        {
            Field theUnsafe=Unsafe.class.getDeclaredField("theUnsafe");
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

    //public static Plugin getCallerPlugin(int x)
    //{
    //    return JavaPlugin.getProvidingPlugin(getCallerClass(x+1));
    //}

    //public static Plugin getCallerPlugin()
    //{
    //    return getCallerPlugin(1);
    //}

    public static Class<?> getCallerClass(int x)
    {
        return getClass(getCaller(x).getClassName());
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
        return getCallers().get(x);
    }

    public static List<StackTraceElement> getCallers()
    {
        var list=new ArrayList<StackTraceElement>();
        for (var o: Thread.currentThread().getStackTrace())
        {
            //去掉反射调用和内部调用
            String className=o.getClassName();
            if (className.startsWith("java.lang.reflect.")||className.startsWith("sun.reflect.")||className.startsWith(
                    ReflectAccessor.class.getPackageName())||className.startsWith("jdk.internal."))
            {
                continue;
            }
            list.add(o);
        }
        return list;
    }

    public static boolean isVisibility(Class<?> clazz)
    {
        return !clazz.getPackage().equals(ReflectAccessor.class.getPackage());
    }

    public static <T> T blankInstance(Class<T> type) throws Throwable
    {
        return Objects.requireNonNull(ConstructorAccessor.createBlankConstructor(type)).invoke((Object) null);
    }

    public static List<Class<?>> getAllSuperClass(Class<?> type)
    {
        List<Class<?>> clazz=new ArrayList<>();
        if (type==null)
        {
            return clazz;
        }
        while (type!=null&&type!=Object.class)
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
        Arrays.asList(c.getDeclaredFields()).forEach(field -> accessors.add(new FieldAccessor(field)));
        return accessors;
    }

    public static List<FieldAccessor> getDeclaredFields(Class<?> c)
    {
        List<FieldAccessor> accessors=new ArrayList<>();
        for (var cc: getAllSuperClass(c))
        {
            accessors.addAll(getFields(cc));
        }
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
        return FieldAccessor.getDeclaredFields(getBothSuperClass(a,b));
    }

    public static boolean isSimpleObject(Object o)
    {
        Class<?> type=o.getClass();
        return type.isPrimitive()||type.equals(String.class)||type.equals(Long.class)||type.equals(Boolean.class)||type.equals(
                Short.class)||type.equals(Integer.class)||type.equals(Character.class)||type.equals(Float.class)||type.equals(
                Double.class)||type.equals(Void.class)||type.equals(Byte.class)||type.isEnum()||type==Class.class||type==Method.class||type==Constructor.class;
    }

    public static <T> T cloneObject(T o) throws Throwable
    {
        if (null==o)
        {
            return null;
        }
        Map<Object,Object> map=new HashMap<>();
        return cloneObject(o,map);
    }

    private static <T> T cloneObject(T o,Map<Object,Object> map) throws Throwable
    {
        if (o==null)
        {
            return null;
        }
        if (isSimpleObject(o))
        {
            return o;
        }
        Object newInstance=map.get(o);
        if (newInstance!=null)
        {
            return (T) newInstance;
        }

        if (o.getClass().isArray())
        {
            newInstance=cloneArray(o,map);
        }
        else
        {
            newInstance=blankInstance(o.getClass());
        }
        map.put(o,newInstance);
        cloneFields(o,newInstance,map);
        return (T) newInstance;
    }

    private static Object cloneArray(Object o,Map<Object,Object> map) throws Throwable
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
            if (Array.get(o,i)==null) continue;
            var x=cloneObject(Array.get(o,i),map);
            Array.set(array,i,x);
        }
        return array;
    }

    private static void cloneFields(Object object,Object newObject,Map<Object,Object> map) throws Throwable
    {
        if (object==null||newObject==null)
        {
            return;
        }
        List<FieldAccessor> fields=getDeclaredFields(object.getClass());
        for (FieldAccessor f: fields)
        {
            if (!Modifier.isStatic(f.getField().getModifiers()))
            {
                f.set(newObject,cloneObject(f.get(object),map));
            }
        }
    }

    public static <T> T objectCopy(T o,T newObject) throws Throwable
    {
        Map<Object,Object> map=new HashMap<>();
        map.put(o,newObject);
        cloneFields(o,newObject,map);
        return o;
    }
}
