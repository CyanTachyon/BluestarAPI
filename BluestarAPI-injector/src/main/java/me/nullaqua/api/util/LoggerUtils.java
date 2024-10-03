package me.nullaqua.api.util;

import me.nullaqua.api.util.function.RunWithThrow;

import java.util.function.BooleanSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logger Utils
 * @author nullaqua
 */
@SuppressWarnings("unused")
public class LoggerUtils
{
    private final Logger logger;

    public LoggerUtils(Logger logger)
    {
        this.logger=logger;
    }

    public Logger logger()
    {
        return logger;
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
     * Log a SEVERE message with a throwable.
     * <p>
     * If the logger is currently enabled for the SEVERE message
     * level then the given message and throwable are forwarded to all the
     * registered output Handler objects.
     *
     * @param msg The string message (or a key in the message catalog)
     */
    public void severe(String msg,Throwable t)
    {
        logger.log(Level.SEVERE,msg,t);
    }

    /**
     * Log a SEVERE message with a runnable.
     * <p>
     * Execute the runnable and log the message if the runnable throws an exception.
     *
     * @param run The runnable to be executed
     * @param msg The string message (or a key in the message catalog)
     */
    public void severe(RunWithThrow run,String msg)
    {
        run(run,Level.SEVERE,msg);
    }

    /**
     * Log a SEVERE message with a boolean supplier.
     * <p>
     * Execute the boolean supplier and log the message if the supplier returns true.
     *
     * @param supplier The boolean supplier to be executed
     * @param msg      The string message (or a key in the message catalog)
     */
    public void severe(BooleanSupplier supplier,String msg)
    {
        run(supplier,Level.SEVERE,msg);
    }

    /**
     * Log a WARNING message.
     * <p>
     * If the logger is currently enabled for the WARNING message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     *
     * @param msg The string message (or a key in the message catalog)
     */
    public void warning(String msg)
    {
        logger.warning(msg);
    }

    /**
     * Log a WARNING message with a throwable.
     * <p>
     * If the logger is currently enabled for the WARNING message
     * level then the given message and throwable are forwarded to all the
     * registered output Handler objects.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param t   The throwable associated with the message
     */
    public void warning(String msg,Throwable t)
    {
        logger.log(Level.WARNING,msg,t);
    }

    /**
     * Log a WARNING message with a runnable.
     * <p>
     * Execute the runnable and log the message if the runnable throws an exception.
     *
     * @param run The runnable to be executed
     * @param msg The string message (or a key in the message catalog)
     */
    public void warning(RunWithThrow run,String msg)
    {
        run(run,Level.WARNING,msg);
    }

    /**
     * Log a WARNING message with a boolean supplier.
     * <p>
     * Execute the boolean supplier and log the message if the supplier returns true.
     *
     * @param supplier The boolean supplier to be executed
     * @param msg      The string message (or a key in the message catalog)
     */
    public void warning(BooleanSupplier supplier,String msg)
    {
        run(supplier,Level.WARNING,msg);
    }

    /**
     * Log an INFO message.
     * <p>
     * If the logger is currently enabled for the INFO message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     *
     * @param msg The string message (or a key in the message catalog)
     */
    public void info(String msg)
    {
        logger.info(msg);
    }

    /**
     * Log an INFO message with a throwable.
     * <p>
     * If the logger is currently enabled for the INFO message
     * level then the given message and throwable are forwarded to all the
     * registered output Handler objects.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param t   The throwable associated with the message
     */
    public void info(String msg,Throwable t)
    {
        logger.log(Level.INFO,msg,t);
    }

    /**
     * Log an INFO message with a runnable.
     * <p>
     * Execute the runnable and log the message if the runnable throws an exception.
     *
     * @param run The runnable to be executed
     * @param msg The string message (or a key in the message catalog)
     */
    public void info(RunWithThrow run,String msg)
    {
        run(run,Level.INFO,msg);
    }

    /**
     * Log an INFO message with a boolean supplier.
     * <p>
     * Execute the boolean supplier and log the message if the supplier returns true.
     *
     * @param supplier The boolean supplier to be executed
     * @param msg      The string message (or a key in the message catalog)
     */
    public void info(BooleanSupplier supplier,String msg)
    {
        run(supplier,Level.INFO,msg);
    }

    /**
     * Log a CONFIG message.
     * <p>
     * If the logger is currently enabled for the CONFIG message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     *
     * @param msg The string message (or a key in the message catalog)
     */
    public void config(String msg)
    {
        logger.config(msg);
    }

    /**
     * Log a CONFIG message with a throwable.
     * <p>
     * If the logger is currently enabled for the CONFIG message
     * level then the given message and throwable are forwarded to all the
     * registered output Handler objects.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param t The throwable associated with the message
     */
    public void config(String msg,Throwable t)
    {
        logger.log(Level.CONFIG,msg,t);
    }

    /**
     * Log a CONFIG message with a runnable.
     * <p>
     * Execute the runnable and log the message if the runnable throws an exception.
     *
     * @param run The runnable to be executed
     * @param msg The string message (or a key in the message catalog)
     */
    public void config(RunWithThrow run,String msg)
    {
        run(run,Level.CONFIG,msg);
    }

    /**
     * Log a CONFIG message with a boolean supplier.
     * <p>
     * Execute the boolean supplier and log the message if the supplier returns true.
     *
     * @param supplier The boolean supplier to be executed
     * @param msg The string message (or a key in the message catalog)
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
     * Execute the runnable and log the message if the runnable throws an exception.
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
     * Execute the boolean supplier and log the message if the supplier returns true.
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
     * Execute the runnable and log the message if the runnable throws an exception.
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
     * Execute the boolean supplier and log the message if the supplier returns true.
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
     * Execute the runnable and log the message if the runnable throws an exception.
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
     * Execute the boolean supplier and log the message if the supplier returns true.
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
