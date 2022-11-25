package me.lanzhi.api.reflect;

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

        MethodHandle target=null;
        MethodType methodType=null;
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
