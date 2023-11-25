package me.nullaqua.api.io.file;

import me.nullaqua.api.io.IOStreamKey;
import me.nullaqua.api.io.KeyObjectInputStream;
import me.nullaqua.api.reflect.MethodAccessor;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.Objects;

public class FileWithVersionReader
{
    private final File file;

    public FileWithVersionReader(File file)
    {
        this.file=file;
    }

    public File file()
    {
        return file;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(file);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj==this)
        {
            return true;
        }
        if (obj==null||obj.getClass()!=this.getClass())
        {
            return false;
        }
        var that=(FileWithVersionReader) obj;
        return Objects.equals(this.file,that.file);
    }

    @Override
    public String toString()
    {
        return "FileWithVersionReader["+"file="+file+']';
    }

    public boolean read(Worker worker)
    {
        return this.read(worker,IOStreamKey.EmptyKey);
    }

    public boolean read(Worker worker,IOStreamKey key)
    {
        return read(worker,key,IOStreamKey.EmptyKey);
    }

    public boolean read(Worker worker,IOStreamKey key,IOStreamKey baseKey)
    {
        try (var stream=KeyObjectInputStream.create(Files.newInputStream(file.toPath()),baseKey))
        {
            var version=(String) stream.readObject(key);
            var method=getWorkMethod(worker,version);
            if (method==null)
            {
                worker.defaultRead(version,stream);
            }
            else
            {
                method.invoke(worker,stream);
            }
            return true;
        }
        catch (Throwable ignored)
        {
        }
        try (var stream=KeyObjectInputStream.create(Files.newInputStream(file.toPath()),baseKey))
        {
            worker.defaultRead(null,stream);
            return true;
        }
        catch (Throwable ignored)
        {
        }
        return false;
    }

    private static MethodAccessor getWorkMethod(Worker worker,String ver)
    {
        for (Method method: worker.getClass().getDeclaredMethods())
        {
            var readVersion=method.getAnnotation(ReadVersion.class);
            if (readVersion!=null&&
                readVersion.value().equals(ver)&&
                method.getParameterTypes().length==1&&
                method.getParameterTypes()[0].equals(KeyObjectInputStream.class))
            {
                return new MethodAccessor(method);
            }
        }
        return null;
    }

    public interface Worker
    {
        default void defaultRead(String version,KeyObjectInputStream stream)
        {
            throw new UnknownFileVersionException();
        }
    }
}