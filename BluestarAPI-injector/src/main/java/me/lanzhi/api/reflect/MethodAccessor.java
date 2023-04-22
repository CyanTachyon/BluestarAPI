package me.lanzhi.api.reflect;

import me.lanzhi.api.collection.FastLinkedList;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import static me.lanzhi.api.reflect.ReflectAccessor.LOOKUP;

public final class MethodAccessor
{
    private final Method method;
    private final boolean staticMethod;
    private final MethodHandle methodHandle;

    public MethodAccessor(Method method)
    {
        if (method==null||!ReflectAccessor.isVisibility(method.getDeclaringClass()))
        {
            this.method=null;
            this.methodHandle=null;
            this.staticMethod=false;
            return;
        }
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
        for (Class<?> clazz: ReflectAccessor.getAllSuperClass(c))
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
        List<MethodAccessor> list=new FastLinkedList<>();
        for (Method method: c.getDeclaredMethods())
        {
            list.add(new MethodAccessor(method));
        }
        c=c.getSuperclass();
        for (Class<?> clazz: ReflectAccessor.getAllSuperClass(c))
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

    public Object invoke(Object target,Object... args) throws Throwable
    {
        if (methodHandle==null)
        {
            if (!ReflectAccessor.isVisibility(method.getDeclaringClass()))
            {
                return null;
            }
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
