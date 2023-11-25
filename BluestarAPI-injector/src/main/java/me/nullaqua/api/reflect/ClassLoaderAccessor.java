package me.nullaqua.api.reflect;

import java.nio.ByteBuffer;
import java.security.ProtectionDomain;

/**
 * 类加载器访问器
 */
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

    /**
     * 构造函数
     *
     * @param classLoader 类加载器
     */
    public ClassLoaderAccessor(ClassLoader classLoader)
    {
        this.classLoader=classLoader;
    }

    /**
     * 创建ClassLoaderAccessor实例
     *
     * @param classLoader 类加载器
     * @return ClassLoaderAccessor实例
     */
    public static ClassLoaderAccessor of(ClassLoader classLoader)
    {
        if (classLoader instanceof java.net.URLClassLoader)
        {
            return new URLClassLoaderAccessor((java.net.URLClassLoader) classLoader);
        }
        return new ClassLoaderAccessor(classLoader);
    }

    /**
     * 获取类加载器
     *
     * @return 类加载器
     */
    public ClassLoader classLoader()
    {
        return classLoader;
    }

    /**
     * 添加类
     *
     * @param clazz 类
     * @throws Throwable 异常
     */
    public void addClass(Class<?> clazz) throws Throwable
    {
        addClass.invoke(classLoader,clazz);
    }

    /**
     * 定义类
     *
     * @param bytes  字节数组
     * @param offset 偏移量
     * @param length 长度
     * @return 类
     * @throws Throwable 异常
     */
    public Class<?> defineClass(byte[] bytes,int offset,int length) throws Throwable
    {
        return (Class<?>) defineClass.invoke(classLoader,bytes,offset,length);
    }

    /**
     * 定义类
     *
     * @param name   类名
     * @param bytes  字节数组
     * @param offset 偏移量
     * @param length 长度
     * @return 类
     * @throws Throwable 异常
     */
    public Class<?> defineClass(String name,byte[] bytes,int offset,int length) throws Throwable
    {
        return (Class<?>) defineClass2.invoke(classLoader,name,bytes,offset,length);
    }

    /**
     * 定义类
     *
     * @param name             类名
     * @param bytes            字节数组
     * @param offset           偏移量
     * @param length           长度
     * @param protectionDomain 保护域
     * @return 类
     * @throws Throwable 异常
     */
    public Class<?> defineClass(String name,byte[] bytes,int offset,int length,ProtectionDomain protectionDomain) throws
            Throwable
    {
        return (Class<?>) defineClass3.invoke(classLoader,name,bytes,offset,length,protectionDomain);
    }

    /**
     * 定义类
     *
     * @param name             类名
     * @param byteBuffer       字节缓冲区
     * @param protectionDomain 保护域
     * @return 类
     * @throws Throwable 异常
     */
    public Class<?> defineClass(String name,ByteBuffer byteBuffer,ProtectionDomain protectionDomain) throws Throwable
    {
        return (Class<?>) defineClass4.invoke(classLoader,name,byteBuffer,protectionDomain);
    }
}