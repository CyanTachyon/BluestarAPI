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
    inline fun <T> severe(msg: String, block: () -> T) = logOnFailure(Level.SEVERE, msg, block)

    /**
     * Log a WARNING message with a runnable.
     *
     *
     * Execute the runnable and log the message if the runnable throws an exception.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The runnable to be executed
     */
    inline fun <T> warning(msg: String, block: () -> T) = logOnFailure(Level.WARNING, msg, block)

    /**
     * Log a INFO message with a runnable.
     *
     *
     * Execute the runnable and log the message if the runnable throws an exception.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The runnable to be executed
     */
    inline fun <T> info(msg: String, block: () -> T) = logOnFailure(Level.INFO, msg, block)

    /**
     * Log a CONFIG message with a runnable.
     *
     *
     * Execute the runnable and log the message if the runnable throws an exception.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The runnable to be executed
     */
    inline fun <T> config(msg: String, block: () -> T) = logOnFailure(Level.CONFIG, msg, block)

    /**
     * Log a FINE message with a runnable.
     *
     *
     * Execute the runnable and log the message if the runnable throws an exception.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The runnable to be executed
     */
    inline fun <T> fine(msg: String, block: () -> T) = logOnFailure(Level.FINE, msg, block)

    /**
     * Log a FINER message with a runnable.
     *
     *
     * Execute the runnable and log the message if the runnable throws an exception.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The runnable to be executed
     */
    inline fun <T> finer(msg: String, block: () -> T) = logOnFailure(Level.FINER, msg, block)

    /**
     * Log a FINEST message with a runnable.
     *
     *
     * Execute the runnable and log the message if the runnable throws an exception.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The runnable to be executed
     */
    inline fun <T> finest(msg: String, block: () -> T) = logOnFailure(Level.FINEST, msg, block)

    inline fun <T> logOnFailure(level: Level, msg: String, block: () -> T) =
        runCatching(block).onFailure { logger.log(level, msg, it) }
}