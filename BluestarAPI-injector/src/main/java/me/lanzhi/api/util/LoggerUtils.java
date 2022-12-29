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

    public LoggerUtils(Logger logger)
    {
        this.logger=logger;
    }

    public Logger logger()
    {
        return logger;
    }

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

    public void severe(String msg,Throwable t)
    {
        logger.log(Level.SEVERE,msg,t);
    }

    public void severe(RunWithThrow run,String msg)
    {
        run(run,Level.SEVERE,msg);
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

    public void severe(BooleanSupplier supplier,String msg)
    {
        run(supplier,Level.SEVERE,msg);
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

    public void warning(String msg,Throwable t)
    {
        logger.log(Level.WARNING,msg,t);
    }

    public void warning(RunWithThrow run,String msg)
    {
        run(run,Level.WARNING,msg);
    }

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

    public void info(String msg,Throwable t)
    {
        logger.log(Level.INFO,msg,t);
    }

    public void info(RunWithThrow run,String msg)
    {
        run(run,Level.INFO,msg);
    }

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

    public void config(String msg,Throwable t)
    {
        logger.log(Level.CONFIG,msg,t);
    }

    public void config(RunWithThrow run,String msg)
    {
        run(run,Level.CONFIG,msg);
    }

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

    public void fine(String msg,Throwable t)
    {
        logger.log(Level.FINE,msg,t);
    }

    public void fine(RunWithThrow run,String msg)
    {
        run(run,Level.FINE,msg);
    }

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

    public void finer(String msg,Throwable t)
    {
        logger.log(Level.FINER,msg,t);
    }

    public void finer(RunWithThrow run,String msg)
    {
        run(run,Level.FINER,msg);
    }

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

    public void finest(String msg,Throwable t)
    {
        logger.log(Level.FINEST,msg,t);
    }

    public void finest(RunWithThrow run,String msg)
    {
        run(run,Level.FINEST,msg);
    }

    public void finest(BooleanSupplier supplier,String msg)
    {
        run(supplier,Level.FINEST,msg);
    }
}
