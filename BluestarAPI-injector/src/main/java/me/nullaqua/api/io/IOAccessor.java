package me.nullaqua.api.io;

import me.nullaqua.api.BluestarUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public final class IOAccessor implements Serializable
{
    private static final IOStreamKey.HexKey hexKey=new IOStreamKey.HexKey();

    private IOAccessor()
    {
    }

    public static IOStreamKey.XorKey randomXorKey()
    {
        return xorKey(BluestarUtils.randomByte());
    }

    public static IOStreamKey.XorKey xorKey(byte b)
    {
        return new IOStreamKey.XorKey(b);
    }

    public static IOStreamKey.HexKey hexKey()
    {
        return hexKey;
    }

    public static IOStreamKey plusKeys(IOStreamKey... key)
    {
        return IOStreamKey.plus(key);
    }

    public static void copyFile(final File src,final File dest) throws IOException
    {
        Files.newInputStream(src.toPath()).transferTo(Files.newOutputStream(dest.toPath()));
    }

    public static boolean validation(File file,File hashFile)
    {
        return file.exists()&&hashFile.exists()&&readFile(hashFile).startsWith(getHash(file));
    }

    @NotNull
    public static String getHash(File file)
    {
        try
        {
            MessageDigest digest=MessageDigest.getInstance("sha-1");
            try (InputStream inputStream=Files.newInputStream(file.toPath()))
            {
                byte[] buffer=new byte[1024];
                int total;
                while ((total=inputStream.read(buffer))!=-1)
                {
                    digest.update(buffer,0,total);
                }
            }
            return getHash(digest);
        }
        catch (IOException|NoSuchAlgorithmException ex)
        {
            ex.printStackTrace();
        }
        return "null ("+UUID.randomUUID()+")";
    }

    @NotNull
    public static String getHash(MessageDigest digest)
    {
        StringBuilder result=new StringBuilder();
        for (byte b: digest.digest())
        {
            result.append(String.format("%02x",b));
        }
        return result.toString();
    }

    @NotNull
    public static String readFile(File file)
    {
        try (FileInputStream fileInputStream=new FileInputStream(file))
        {
            return readFully(fileInputStream,StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return "null ("+UUID.randomUUID()+")";
    }

    @NotNull
    public static String readFully(InputStream inputStream,Charset charset) throws IOException
    {
        return new String(readFully(inputStream),charset);
    }

    @NotNull
    public static byte[] readFully(InputStream inputStream) throws IOException
    {
        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        byte[] buf=new byte[1024];
        int len;
        while ((len=inputStream.read(buf))>0)
        {
            stream.write(buf,0,len);
        }
        return stream.toByteArray();
    }
}