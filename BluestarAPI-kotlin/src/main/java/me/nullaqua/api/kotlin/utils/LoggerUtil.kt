package me.nullaqua.api.kotlin.utils

import me.nullaqua.api.util.LoggerUtils
import me.nullaqua.api.util.function.RunWithThrow
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
    fun severe(msg: String, block: () -> Unit) = super.severe(RunWithThrow(block), msg)

    /**
     * Log a WARNING message with a runnable.
     *
     *
     * Execute the runnable and log the message if the runnable throws an exception.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The runnable to be executed
     */
    fun warning(msg: String, block: () -> Unit) = super.warning(RunWithThrow(block), msg)

    /**
     * Log a INFO message with a runnable.
     *
     *
     * Execute the runnable and log the message if the runnable throws an exception.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The runnable to be executed
     */
    fun info(msg: String, block: () -> Unit) = super.info(RunWithThrow(block), msg)

    /**
     * Log a CONFIG message with a runnable.
     *
     *
     * Execute the runnable and log the message if the runnable throws an exception.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The runnable to be executed
     */
    fun config(msg: String, block: () -> Unit) = super.config(RunWithThrow(block), msg)

    /**
     * Log a FINE message with a runnable.
     *
     *
     * Execute the runnable and log the message if the runnable throws an exception.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The runnable to be executed
     */
    fun fine(msg: String, block: () -> Unit) = super.fine(RunWithThrow(block), msg)

    /**
     * Log a FINER message with a runnable.
     *
     *
     * Execute the runnable and log the message if the runnable throws an exception.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The runnable to be executed
     */
    fun finer(msg: String, block: () -> Unit) = super.finer(RunWithThrow(block), msg)

    /**
     * Log a FINEST message with a runnable.
     *
     *
     * Execute the runnable and log the message if the runnable throws an exception.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The runnable to be executed
     */
    fun finest(msg: String, block: () -> Unit) = super.finest(RunWithThrow(block), msg)
}