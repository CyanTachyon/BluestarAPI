package me.lanzhi.api.reflect;

import java.net.URLClassLoader;

public class URLClassLoaderAccessor extends ClassLoaderAccessor
{
    private final static MethodAccessor addURL=MethodAccessor.getMethod(URLClassLoader.class,
                                                                        "addURL",
                                                                        java.net.URL.class);

    public URLClassLoaderAccessor(URLClassLoader classLoader)
    {
        super(classLoader);
    }

    public static URLClassLoaderAccessor of(URLClassLoader classLoader)
    {
        return new URLClassLoaderAccessor(classLoader);
    }

    public URLClassLoaderAccessor addURL(java.net.URL url) throws Throwable
    {
        addURL.invoke(classLoader(),url);
        return this;
    }

    @Override
    public URLClassLoader classLoader()
    {
        return (URLClassLoader) super.classLoader();
    }

    public URLClassLoaderAccessor addURL(String url) throws Throwable
    {
        addURL.invoke(classLoader(),new java.net.URL(url));
        return this;
    }

    public URLClassLoaderAccessor addURL(java.io.File file) throws Throwable
    {
        addURL.invoke(classLoader(),file.toURI().toURL());
        return this;
    }

    public URLClassLoaderAccessor addURL(java.nio.file.Path path) throws Throwable
    {
        addURL.invoke(classLoader(),path.toUri().toURL());
        return this;
    }
}
