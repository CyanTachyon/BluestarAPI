package me.nullaqua.api.io.file;

import java.io.File;

public class UnknownFileVersionException extends RuntimeException
{
    public UnknownFileVersionException(FileWithVersionReader file)
    {
        this(file.file());
    }

    public UnknownFileVersionException(File file)
    {
        this("读取文件时出错: 无法分析文件版本 "+file.getAbsolutePath());
    }

    /**
     * Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public UnknownFileVersionException(String message)
    {
        super(message);
    }

    public UnknownFileVersionException()
    {
        this("读取文件时出错: 无法分析文件版本");
    }
}