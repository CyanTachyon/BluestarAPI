package me.lanzhi.api.io.file;

import me.lanzhi.api.io.IOStreamKey;
import me.lanzhi.api.io.KeyObjectOutputStream;
import me.lanzhi.api.util.function.ConsumerWithThrow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class FileWithVersionWriter
{
    private final File file;

    public FileWithVersionWriter(File file)
    {
        this.file=file;
    }

    public File file()
    {
        return file;
    }

    public void saveFile(String ver,ConsumerWithThrow<KeyObjectOutputStream> consumer) throws IOException
    {
        saveFile(IOStreamKey.EmptyKey,ver,consumer);
    }

    public void saveFile(IOStreamKey key,String ver,ConsumerWithThrow<KeyObjectOutputStream> consumer) throws IOException
    {
        saveFile(key,IOStreamKey.EmptyKey,ver,consumer);
    }

    public void saveFile(IOStreamKey key,IOStreamKey baseKey,String ver,
                         ConsumerWithThrow<KeyObjectOutputStream> consumer) throws IOException
    {
        if (!file.exists())
        {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        if (file.isDirectory())
        {
            throw new IOException("文件保存失败,已有重名文件夹");
        }
        try (var koos=KeyObjectOutputStream.create(new FileOutputStream(file),baseKey))
        {
            koos.writeObject(ver,key);
            if (!Objects.isNull(consumer))
            {
                consumer.accept(koos,IOException.class);
            }
        }
    }
}
