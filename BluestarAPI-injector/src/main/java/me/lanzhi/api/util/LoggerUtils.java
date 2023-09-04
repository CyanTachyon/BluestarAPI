package me.lanzhi.api.util;

import me.lanzhi.api.util.function.RunWithThrow;

import java.util.function.BooleanSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 管理器
 */
public class LoggerUtils
{
    private Logger logger;

    /**
     * 构造方法，用于创建LoggerUtils对象
     *
     * @param logger 日志记录器
     */
    public LoggerUtils(Logger logger)
    {
        this.logger=logger;
    }

    /**
     * 获取日志记录器
     *
     * @return logger 日志记录器
     */
    public Logger logger()
    {
        return logger;
    }

    /**
     * 设置日志记录器
     *
     * @param logger 日志记录器
     */
    public void setLogger(Logger logger)
    {
        this.logger=logger;
    }

    /**
     * Log a SEVERE message.
     * <p>
     * If the logger is currently enabled for the SEVERE message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     *
     * @param msg The string message (or a key in the message catalog)
     */
    public void severe(String msg)
    {
        logger.severe(msg);
    }

    /**
     * 记录严重级别的日志消息和相关的异常信息。
     *
     * @param msg 要记录的日志消息
     * @param t   相关的异常
     */
    public void severe(String msg,Throwable t)
    {
        logger.log(Level.SEVERE,msg,t);
    }

    /**
     * 记录严重级别的日志消息。
     *
     * @param run 要执行的操作
     * @param msg 要记录的日志消息
     */
    public void severe(RunWithThrow run,String msg)
    {
        run(run,Level.SEVERE,msg);
    }

    public void severe(BooleanSupplier supplier,String msg)
    {
        run(supplier,Level.SEVERE,msg);
    }

    /**
     * 记录警告级别的日志消息。
     * <p>
     * 如果日志记录器当前启用了警告级别的消息，则将给定的消息转发给所有已注册的输出处理器对象。
     *
     * @param msg 字符串消息（或消息目录中的键）
     */
    public void warning(String msg)
    {
        logger.warning(msg);
    }

    /**
     * 记录警告级别的日志消息和相关的异常信息。
     *
     * @param msg 要记录的日志消息
     * @param t   相关的异常
     */
    public void warning(String msg,Throwable t)
    {
        logger.log(Level.WARNING,msg,t);
    }

    /**
     * 记录警告级别的日志消息。
     *
     * @param run 要执行的操作
     * @param msg 要记录的日志消息
     */
    public void warning(RunWithThrow run,String msg)
    {
        run(run,Level.WARNING,msg);
    }

    /**
     * 记录警告级别的日志消息。
     *
     * @param supplier 布尔供应商
     * @param msg      要记录的日志消息
     */
    public void warning(BooleanSupplier supplier,String msg)
    {
        run(supplier,Level.WARNING,msg);
    }

    /**
     * 记录信息级别的日志消息。
     * <p>
     * 如果日志记录器当前启用了信息级别的消息，则将给定的消息转发给所有已注册的输出处理器对象。
     *
     * @param msg 字符串消息（或消息目录中的键）
     */
    public void info(String msg)
    {
        logger.info(msg);
    }

    /**
     * 记录信息级别的日志消息和相关的异常信息。
     *
     * @param msg 要记录的日志消息
     * @param t   相关的异常
     */
    public void info(String msg,Throwable t)
    {
        logger.log(Level.INFO,msg,t);
    }

    /**
     * 记录信息级别的日志消息。
     *
     * @param run 要执行的操作
     * @param msg 要记录的日志消息
     */
    public void info(RunWithThrow run,String msg)
    {
        run(run,Level.INFO,msg);
    }

    /**
     * 记录信息级别的日志消息。
     *
     * @param supplier 布尔供应商
     * @param msg      要记录的日志消息
     */
    public void info(BooleanSupplier supplier,String msg)
    {
        run(supplier,Level.INFO,msg);
    }

    /**
     * 记录配置级别的日志消息。
     * <p>
     * 如果日志记录器当前启用了配置级别的消息，则将给定的消息转发给所有已注册的输出处理器对象。
     *
     * @param msg 字符串消息（或消息目录中的键）
     */
    public void config(String msg)
    {
        logger.config(msg);
    }

    /**
     * 记录配置级别的日志消息和相关的异常信息。
     *
     * @param msg 要记录的日志消息
     * @param t   相关的异常
     */
    public void config(String msg,Throwable t)
    {
        logger.log(Level.CONFIG,msg,t);
    }

    /**
     * 记录配置级别的日志消息。
     *
     * @param run 要执行的操作
     * @param msg 要记录的日志消息
     */
    public void config(RunWithThrow run,String msg)
    {
        run(run,Level.CONFIG,msg);
    }

    /**
     * 记录配置级别的日志消息。
     *
     * @param supplier 布尔供应商
     * @param msg      要记录的日志消息
     */
    public void config(BooleanSupplier supplier,String msg)
    {
        run(supplier,Level.CONFIG,msg);
    }

    /**
     * Log a FINE message.
     * <p>
     * If the logger is currently enabled for the FINE message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     *
     * @param msg The string message (or a key in the message catalog)
     */
    public void fine(String msg)
    {
        logger.fine(msg);
    }

    /**
     * Log a FINE message with a throwable.
     * <p>
     * If the logger is currently enabled for the FINE message
     * level then the given message and throwable are forwarded to all the
     * registered output Handler objects.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param t   The throwable associated with the message
     */
    public void fine(String msg,Throwable t)
    {
        logger.log(Level.FINE,msg,t);
    }

    /**
     * Log a FINE message with a runnable.
     * <p>
     * If the logger is currently enabled for the FINE message
     * level then the given runnable is executed and the resulting message
     * is forwarded to all the registered output Handler objects.
     *
     * @param run The runnable to be executed
     * @param msg The string message (or a key in the message catalog)
     */
    public void fine(RunWithThrow run,String msg)
    {
        run(run,Level.FINE,msg);
    }

    /**
     * Log a FINE message with a boolean supplier.
     * <p>
     * If the logger is currently enabled for the FINE message
     * level then the given boolean supplier is executed and the resulting message
     * is forwarded to all the registered output Handler objects.
     *
     * @param supplier The boolean supplier to be executed
     * @param msg      The string message (or a key in the message catalog)
     */
    public void fine(BooleanSupplier supplier,String msg)
    {
        run(supplier,Level.FINE,msg);
    }

    /**
     * Log a FINER message.
     * <p>
     * If the logger is currently enabled for the FINER message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     *
     * @param msg The string message (or a key in the message catalog)
     */
    public void finer(String msg)
    {
        logger.finer(msg);
    }

    /**
     * Log a FINER message with a throwable.
     * <p>
     * If the logger is currently enabled for the FINER message
     * level then the given message and throwable are forwarded to all the
     * registered output Handler objects.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param t   The throwable associated with the message
     */
    public void finer(String msg,Throwable t)
    {
        logger.log(Level.FINER,msg,t);
    }

    /**
     * Log a FINER message with a runnable.
     * <p>
     * If the logger is currently enabled for the FINER message
     * level then the given runnable is executed and the resulting message
     * is forwarded to all the registered output Handler objects.
     *
     * @param run The runnable to be executed
     * @param msg The string message (or a key in the message catalog)
     */
    public void finer(RunWithThrow run,String msg)
    {
        run(run,Level.FINER,msg);
    }

    /**
     * Log a FINER message with a boolean supplier.
     * <p>
     * If the logger is currently enabled for the FINER message
     * level then the given boolean supplier is executed and the resulting message
     * is forwarded to all the registered output Handler objects.
     *
     * @param supplier The boolean supplier to be executed
     * @param msg      The string message (or a key in the message catalog)
     */
    public void finer(BooleanSupplier supplier,String msg)
    {
        run(supplier,Level.FINER,msg);
    }

    /**
     * Log a FINEST message.
     * <p>
     * If the logger is currently enabled for the FINEST message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     *
     * @param msg The string message (or a key in the message catalog)
     */
    public void finest(String msg)
    {
        logger.finest(msg);
    }

    /**
     * Log a FINEST message with a throwable.
     * <p>
     * If the logger is currently enabled for the FINEST message
     * level then the given message and throwable are forwarded to all the
     * registered output Handler objects.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param t   The throwable associated with the message
     */
    public void finest(String msg,Throwable t)
    {
        logger.log(Level.FINEST,msg,t);
    }

    /**
     * Log a FINEST message with a runnable.
     * <p>
     * If the logger is currently enabled for the FINEST message
     * level then the given runnable is executed and the resulting message
     * is forwarded to all the registered output Handler objects.
     *
     * @param run The runnable to be executed
     * @param msg The string message (or a key in the message catalog)
     */
    public void finest(RunWithThrow run,String msg)
    {
        run(run,Level.FINEST,msg);
    }

    /**
     * Log a FINEST message with a boolean supplier.
     * <p>
     * If the logger is currently enabled for the FINEST message
     * level then the given boolean supplier is executed and the resulting message
     * is forwarded to all the registered output Handler objects.
     *
     * @param supplier The boolean supplier to be executed
     * @param msg      The string message (or a key in the message catalog)
     */
    public void finest(BooleanSupplier supplier,String msg)
    {
        run(supplier,Level.FINEST,msg);
    }

    private void run(RunWithThrow run,Level level,String msg)
    {
        try
        {
            run.run();
        }
        catch (Throwable e)
        {
            logger.log(level,msg,e);
        }
    }

    private void run(BooleanSupplier supplier,Level level,String msg)
    {
        try
        {
            if (!supplier.getAsBoolean())
            {
                return;
            }
        }
        catch (Throwable e)
        {
            logger.log(level,msg,e);
            return;
        }
        logger.log(level,msg);
    }
}
