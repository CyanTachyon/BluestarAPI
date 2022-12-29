package me.lanzhi.api.reflect;

import java.nio.ByteBuffer;
import java.security.ProtectionDomain;

public class ClassLoaderAccessor
{
    private final static MethodAccessor addClass=MethodAccessor.getMethod(ClassLoader.class,"addClass",Class.class);
    private final static MethodAccessor defineClass=MethodAccessor.getMethod(ClassLoader.class,
                                                                             "defineClass",
                                                                             byte[].class,
                                                                             int.class,
                                                                             int.class);
    private final static MethodAccessor defineClass2=MethodAccessor.getMethod(ClassLoader.class,
                                                                              "defineClass",
                                                                              String.class,
                                                                              byte[].class,
                                                                              int.class,
                                                                              int.class);
    private final static MethodAccessor defineClass3=MethodAccessor.getMethod(ClassLoader.class,
                                                                              "defineClass",
                                                                              String.class,
                                                                              byte[].class,
                                                                              int.class,
                                                                              int.class,
                                                                              ProtectionDomain.class);
    private final static MethodAccessor defineClass4=MethodAccessor.getMethod(ClassLoader.class,
                                                                              "defineClass",
                                                                              String.class,
                                                                              ByteBuffer.class,
                                                                              ProtectionDomain.class);

    private final ClassLoader classLoader;

    public ClassLoaderAccessor(ClassLoader classLoader)
    {
        this.classLoader=classLoader;
    }

    public static ClassLoaderAccessor of(ClassLoader classLoader)
    {
        if (classLoader instanceof java.net.URLClassLoader)
        {
            return new URLClassLoaderAccessor((java.net.URLClassLoader) classLoader);
        }
        return new ClassLoaderAccessor(classLoader);
    }

    public ClassLoader classLoader()
    {
        return classLoader;
    }

    public void addClass(Class<?> clazz) throws Throwable
    {
        addClass.invoke(classLoader,clazz);
    }

    public Class<?> defineClass(byte[] bytes,int offset,int length) throws Throwable
    {
        return (Class<?>) defineClass.invoke(classLoader,bytes,offset,length);
    }

    public Class<?> defineClass(String name,byte[] bytes,int offset,int length) throws Throwable
    {
        return (Class<?>) defineClass2.invoke(classLoader,name,bytes,offset,length);
    }

    public Class<?> defineClass(String name,byte[] bytes,int offset,int length,ProtectionDomain protectionDomain) throws Throwable
    {
        return (Class<?>) defineClass3.invoke(classLoader,name,bytes,offset,length,protectionDomain);
    }

    public Class<?> defineClass(String name,ByteBuffer byteBuffer,ProtectionDomain protectionDomain) throws Throwable
    {
        return (Class<?>) defineClass4.invoke(classLoader,name,byteBuffer,protectionDomain);
    }
}
