package me.lanzhi.api.reflect;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

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

    public static <T> T invoke(Class<?> clazz,Class<T> retuenType,Object o,Object... par) throws InvocationTargetException, IllegalAccessException
    {
        Class<?>[] parClass=new Class[par.length];
        for (int i=0;i<par.length;i++)
        {
            if (par[i]!=null)
            {
                parClass[i]=par.getClass();
            }
        }
        for (Method method: clazz.getDeclaredMethods())
        {
            if (Arrays.equals(method.getParameterTypes(),parClass)&&method.getReturnType()==retuenType)
            {
                return (T) method.invoke(o,par);
            }
        }
        return null;
    }

    public static <T> T fieldCopy(T a,String name,T b)
    {
        try
        {
            FieldAccessor accessor=new FieldAccessor(a.getClass().getDeclaredField(name));
            accessor.set(a,accessor.get(b));
        }
        catch (Exception e)
        {
        }
        return a;
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

    public static Class<?> getCallerClass()
    {
        return getClass(Thread.currentThread().getStackTrace()[2].getClassName());
    }

    public static <T> T objectCopy(T a,T b)
    {
        if (a.getClass().isAssignableFrom(b.getClass()))
        {
            return objectCopy(a,b,a.getClass());
        }
        else if (b.getClass().isAssignableFrom(a.getClass()))
        {
            return objectCopy(a,b,b.getClass());
        }
        return a;
    }

    private static <T> T objectCopy(T a,T b,Class<?> tClass)
    {
        for (Field field: tClass.getDeclaredFields())
        {
            FieldAccessor accessor=new FieldAccessor(field);
            accessor.set(a,accessor.get(b));
        }
        return a;
    }
}
