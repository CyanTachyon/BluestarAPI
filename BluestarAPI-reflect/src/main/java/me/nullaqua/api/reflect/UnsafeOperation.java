package me.nullaqua.api.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static me.nullaqua.api.reflect.ReflectionAccessor.LOOKUP;

@SuppressWarnings("unused")
public class UnsafeOperation
{
    UnsafeOperation(){}
    private static final MethodAccessor generateMethod;
    private static final Object methodAccessorGenerator;

    static
    {
        try
        {
            Class<?> c = Class.forName("jdk.internal.reflect.MethodAccessorGenerator");
            Constructor<?> con = c.getDeclaredConstructor();
            ConstructorAccessor<?> conAccessor = new ConstructorAccessor<>(con);
            Method method = c.getDeclaredMethod(
                "generateMethod",
                Class.class,
                String.class,
                Class[].class,
                Class.class,
                Class[].class,
                int.class
            );
            generateMethod = new MethodAccessor(method);
            methodAccessorGenerator = conAccessor.invoke();
        }
        catch (Throwable e)
        {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static MethodAccessor getInitMethod(ConstructorAccessor<?> constructor) throws Throwable
    {
        Object m = generateMethod.invokeMethod(
            methodAccessorGenerator,
            constructor.getConstructor().getDeclaringClass(),
            "<init>",
            constructor.getConstructor().getParameterTypes(),
            void.class,
            constructor.getConstructor().getExceptionTypes(),
            constructor.getConstructor().getModifiers()
        );
        return new MethodAccessor(false, m);
    }

    public static MethodAccessor getClinitMethod(Class<?> clazz) throws Throwable
    {
        final var h = LOOKUP.unreflect(UnsafeOperation.class.getDeclaredMethod("initClass", Class.class));
        return new MethodAccessor(true, h);
    }

    private static void initClass(Class<?> clazz) throws Throwable
    {
        Class.forName(clazz.getName(), true, clazz.getClassLoader());
    }
}