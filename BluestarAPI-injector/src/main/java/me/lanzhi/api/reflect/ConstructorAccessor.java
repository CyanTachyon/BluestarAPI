package me.lanzhi.api.reflect;

import sun.reflect.ReflectionFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;

import static me.lanzhi.api.reflect.Accessor.LOOKUP;

public final class ConstructorAccessor<T>
{
    private final Constructor<T> constructor;
    private final MethodHandle constructorAccessor;

    public ConstructorAccessor(Constructor<T> constructor)
    {
        if (constructor==null||!Accessor.isVisibility(constructor.getDeclaringClass()))
        {
            this.constructor=null;
            constructorAccessor=null;
            return;
        }

        MethodHandle target;
        MethodType methodType;
        try
        {
            MethodHandle unreflected=LOOKUP.unreflectConstructor(constructor);
            target=unreflected.asFixedArity();
            int paramCount=unreflected.type().parameterCount();
            methodType=MethodType.genericMethodType(0,true);
            target=target.asSpreader(Object[].class,paramCount);
        }
        catch (Exception e)
        {
            this.constructor=constructor;
            this.constructorAccessor=null;
            return;
        }


        this.constructor=constructor;
        this.constructorAccessor=target.asType(methodType);
    }

    public static <T> ConstructorAccessor<T> getConstructor(Class<T> c,Class<?>... classes)
    {
        if (c==null)
        {
            return null;
        }
        classes=classes==null?new Class[0]:classes;
        try
        {
            return new ConstructorAccessor<>(c.getConstructor(classes));
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static <T> ConstructorAccessor<T> getDeclaredConstructor(Class<T> c,Class<?>... classes)
    {
        if (c==null)
        {
            return null;
        }
        classes=classes==null?new Class[0]:classes;
        try
        {
            return new ConstructorAccessor<>(c.getDeclaredConstructor(classes));
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static <T> ConstructorAccessor<T> getOrCreateConstructor(Class<T> type)
    {
        ConstructorAccessor<T> res;
        res=getDeclaredConstructor(type);
        if (res!=null)
        {
            return res;
        }
        ConstructorAccessor<?> accessor;
        for (Class<?> c: Accessor.getAllSuperClass(type))
        {
            accessor=getDeclaredConstructor(c);
            if (accessor!=null)
            {
                return (ConstructorAccessor<T>) createConstructor(type,accessor);
            }
        }
        return (ConstructorAccessor<T>) createConstructor(type,(Constructor<?>) null);
    }

    private static ConstructorAccessor<?> createConstructor(Class<?> t,ConstructorAccessor<?> constructor)
    {
        return createConstructor(t,constructor!=null?constructor.getConstructor():null);
    }

    private static ConstructorAccessor<?> createConstructor(Class<?> t,Constructor<?> constructor)
    {
        if (constructor==null)
        {
            constructor=ConstructorAccessor.getDeclaredConstructor(Object.class).getConstructor();
        }
        ReflectionFactory reflectionFactory=ReflectionFactory.getReflectionFactory();
        constructor=reflectionFactory.newConstructorForSerialization(t,constructor);
        return new ConstructorAccessor<>(constructor);
    }

    public T invoke(Object... args) throws Throwable
    {
        if (constructorAccessor==null)
        {
            if (!Accessor.isVisibility(constructor.getDeclaringClass()))
            {
                return null;
            }
            constructor.setAccessible(true);
            T o=constructor.newInstance(args);
            constructor.setAccessible(false);
            return o;
        }
        return (T) this.constructorAccessor.invokeExact(args);
    }

    public Constructor<T> getConstructor()
    {
        return this.constructor;
    }
}
