package me.nullaqua.api.kotlin.utils

import me.nullaqua.api.util.LoggerUtils
import java.util.logging.Level
import java.util.logging.Logger

@Suppress("unused")
class LoggerUtil(logger: Logger): LoggerUtils(logger)
{
    val logger: Logger get() = super.logger()

    /**
     * Log a SEVERE message with a runnable.
     *
     *
     * Execute the runnable and log the message if the runnable throws an exception.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The runnable to be executed
     */
    inline fun severe(msg: String, block: () -> Unit) = logOnFailure(Level.SEVERE, msg, block)

    /**
     * Log a WARNING message with a runnable.
     *
     *
     * Execute the runnable and log the message if the runnable throws an exception.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The runnable to be executed
     */
    inline fun warning(msg: String, block: () -> Unit) = logOnFailure(Level.WARNING, msg, block)

    /**
     * Log a INFO message with a runnable.
     *
     *
     * Execute the runnable and log the message if the runnable throws an exception.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The runnable to be executed
     */
    inline fun info(msg: String, block: () -> Unit) = logOnFailure(Level.INFO, msg, block)

    /**
     * Log a CONFIG message with a runnable.
     *
     *
     * Execute the runnable and log the message if the runnable throws an exception.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The runnable to be executed
     */
    inline fun config(msg: String, block: () -> Unit) = logOnFailure(Level.CONFIG, msg, block)

    /**
     * Log a FINE message with a runnable.
     *
     *
     * Execute the runnable and log the message if the runnable throws an exception.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The runnable to be executed
     */
    inline fun fine(msg: String, block: () -> Unit) = logOnFailure(Level.FINE, msg, block)

    /**
     * Log a FINER message with a runnable.
     *
     *
     * Execute the runnable and log the message if the runnable throws an exception.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The runnable to be executed
     */
    inline fun finer(msg: String, block: () -> Unit) = logOnFailure(Level.FINER, msg, block)

    /**
     * Log a FINEST message with a runnable.
     *
     *
     * Execute the runnable and log the message if the runnable throws an exception.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The runnable to be executed
     */
    inline fun finest(msg: String, block: () -> Unit) = logOnFailure(Level.FINEST, msg, block)

    inline fun logOnFailure(level: Level, msg: String, block: () -> Unit)
    {
        runCatching(block).onFailure { logger.log(level, msg, it) }
    }
}