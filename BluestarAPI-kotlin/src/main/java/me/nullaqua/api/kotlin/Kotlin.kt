@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "NOTHING_TO_INLINE", "UNUSED")
@file:JvmName("KotlinAPI")

package me.nullaqua.api.kotlin

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun <R> Any.lock(block: ()->R): R
{
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return synchronized(this, block)
}

inline fun Any.asObject() = this as Object

@Throws(InterruptedException::class)
inline fun Any.wait() = this.asObject().wait()

@Throws(InterruptedException::class)
inline fun Any.wait(millis: Long) = this.asObject().wait(millis)

@Throws(InterruptedException::class)
inline fun Any.wait(millis: Long, nanos: Int) = this.asObject().wait(millis, nanos)
inline fun Any.notify() = this.asObject().notify()
inline fun Any.notifyAll() = this.asObject().notifyAll()