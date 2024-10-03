package me.nullaqua.api.kotlin.utils

import me.nullaqua.api.util.LoggerUtils
import me.nullaqua.api.util.function.RunWithThrow
import java.util.function.BooleanSupplier
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
    @JvmName("severeRunnable")
    fun severe(msg: String, block: () -> Unit) = super.severe(RunWithThrow(block), msg)

    /**
     * Log a SEVERE message with a boolean supplier.
     *
     *
     * Execute the boolean supplier and log the message if the supplier returns true.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The boolean supplier to be executed
     */
    @JvmName("severeBooleanSupplier")
    fun severe(msg: String, block: () -> Boolean) = super.severe(BooleanSupplier(block), msg)

    /**
     * Log a WARNING message with a runnable.
     *
     *
     * Execute the runnable and log the message if the runnable throws an exception.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The runnable to be executed
     */
    @JvmName("warningRunnable")
    fun warning(msg: String, block: () -> Unit) = super.warning(RunWithThrow(block), msg)

    /**
     * Log a WARNING message with a boolean supplier.
     *
     *
     * Execute the boolean supplier and log the message if the supplier returns true.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The boolean supplier to be executed
     */
    @JvmName("warningBooleanSupplier")
    fun warning(msg: String, block: () -> Boolean) = super.warning(BooleanSupplier(block), msg)

    /**
     * Log a INFO message with a runnable.
     *
     *
     * Execute the runnable and log the message if the runnable throws an exception.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The runnable to be executed
     */
    @JvmName("infoRunnable")
    fun info(msg: String, block: () -> Unit) = super.info(RunWithThrow(block), msg)

    /**
     * Log a INFO message with a boolean supplier.
     *
     *
     * Execute the boolean supplier and log the message if the supplier returns true.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The boolean supplier to be executed
     */
    @JvmName("infoBooleanSupplier")
    fun info(msg: String, block: () -> Boolean) = super.info(BooleanSupplier(block), msg)

    /**
     * Log a CONFIG message with a runnable.
     *
     *
     * Execute the runnable and log the message if the runnable throws an exception.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The runnable to be executed
     */
    @JvmName("configRunnable")
    fun config(msg: String, block: () -> Unit) = super.config(RunWithThrow(block), msg)

    /**
     * Log a CONFIG message with a boolean supplier.
     *
     *
     * Execute the boolean supplier and log the message if the supplier returns true.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The boolean supplier to be executed
     */
    @JvmName("configBooleanSupplier")
    fun config(msg: String, block: () -> Boolean) = super.config(BooleanSupplier(block), msg)

    /**
     * Log a FINE message with a runnable.
     *
     *
     * Execute the runnable and log the message if the runnable throws an exception.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The runnable to be executed
     */
    @JvmName("fineRunnable")
    fun fine(msg: String, block: () -> Unit) = super.fine(RunWithThrow(block), msg)

    /**
     * Log a FINE message with a boolean supplier.
     *
     *
     * Execute the boolean supplier and log the message if the supplier returns true.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The boolean supplier to be executed
     */
    @JvmName("fineBooleanSupplier")
    fun fine(msg: String, block: () -> Boolean) = super.fine(BooleanSupplier(block), msg)

    /**
     * Log a FINER message with a runnable.
     *
     *
     * Execute the runnable and log the message if the runnable throws an exception.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The runnable to be executed
     */
    @JvmName("finerRunnable")
    fun finer(msg: String, block: () -> Unit) = super.finer(RunWithThrow(block), msg)

    /**
     * Log a FINER message with a boolean supplier.
     *
     *
     * Execute the boolean supplier and log the message if the supplier returns true.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The boolean supplier to be executed
     */
    @JvmName("finerBooleanSupplier")
    fun finer(msg: String, block: () -> Boolean) = super.finer(BooleanSupplier(block), msg)

    /**
     * Log a FINEST message with a runnable.
     *
     *
     * Execute the runnable and log the message if the runnable throws an exception.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The runnable to be executed
     */
    @JvmName("finestRunnable")
    fun finest(msg: String, block: () -> Unit) = super.finest(RunWithThrow(block), msg)

    /**
     * Log a FINEST message with a boolean supplier.
     *
     *
     * Execute the boolean supplier and log the message if the supplier returns true.
     *
     * @param msg The string message (or a key in the message catalog)
     * @param block The boolean supplier to be executed
     */
    @JvmName("finestBooleanSupplier")
    fun finest(msg: String, block: () -> Boolean) = super.finest(BooleanSupplier(block), msg)
}