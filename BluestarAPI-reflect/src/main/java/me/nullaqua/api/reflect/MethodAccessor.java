package me.nullaqua.api.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static me.nullaqua.api.reflect.ReflectionAccessor.LOOKUP;

public final class MethodAccessor
{
    private final Method method;
    private final boolean staticMethod;
    private final MethodHandle methodHandle;

    public MethodAccessor(Method method)
    {
        Objects.requireNonNull(method);
        ReflectionAccessor.checkVisibility(method.getDeclaringClass());

        MethodHandle unreflected;
        boolean staticMethod=Modifier.isStatic(method.getModifiers());
        try
        {
            unreflected=LOOKUP.unreflect(method);
        }
        catch (Exception e)
        {
            this.method=method;
            this.methodHandle=null;
            this.staticMethod=staticMethod;
            return;
        }

        MethodHandle target=unreflected.asFixedArity();
        int paramCount=unreflected.type().parameterCount()-(staticMethod?0:1);
        MethodType methodType=MethodType.genericMethodType(1,true);
        target=target.asSpreader(Object[].class,paramCount);
        if (staticMethod)
        {
            target=MethodHandles.dropArguments(target,0,Object.class);
        }
        MethodHandle generified=target.asType(methodType);

        this.method=method;
        this.methodHandle=generified;
        this.staticMethod=staticMethod;
    }

    public static MethodAccessor getMethod(Class<?> clazz,String name,Class<?>... classes)
    {
        if (clazz==null||name==null)
        {
            return null;
        }
        classes=classes==null?new Class[0]:classes;
        try
        {
            return new MethodAccessor(clazz.getDeclaredMethod(name,classes));
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static MethodAccessor getDeclaredMethod(Class<?> c,String name,Class<?>... classes)
    {
        if (c==null||name==null)
        {
            return null;
        }
        classes=classes==null?new Class[0]:classes;
        for (Class<?> clazz: ReflectionAccessor.getAllSuperClass(c))
        {
            try
            {
                return new MethodAccessor(clazz.getDeclaredMethod(name,classes));
            }
            catch (Exception ignored)
            {
            }
        }
        return null;
    }

    public static List<MethodAccessor> getDeclaredMethods(Class<?> c)
    {
        List<MethodAccessor> list=new ArrayList<>();
        for (Method method: c.getDeclaredMethods())
        {
            list.add(new MethodAccessor(method));
        }
        c=c.getSuperclass();
        for (Class<?> clazz: ReflectionAccessor.getAllSuperClass(c))
        {
            for (Method method:clazz.getDeclaredMethods())
            {
                if (!Modifier.isStatic(method.getModifiers()))
                {
                    list.add(new MethodAccessor(method));
                }
            }
        }
        return list;
    }

    public static List<MethodAccessor> getDeclaredMethods(Object o)
    {
        if (o==null)
        {
            return Collections.emptyList();
        }
        return getDeclaredMethods(o.getClass());
    }

    public Object invoke(Object target,Object... args) throws Throwable
    {
        if (methodHandle==null)
        {
            ReflectionAccessor.checkVisibility(method.getDeclaringClass());
            method.setAccessible(true);
            Object o=method.invoke(target,args);
            method.setAccessible(false);
            return o;
        }
        return this.methodHandle.invoke(target,args);
    }

    public Method getMethod()
    {
        return this.method;
    }

    public boolean isStaticMethod()
    {
        return staticMethod;
    }
}
