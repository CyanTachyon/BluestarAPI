@file:JvmName("Reflect")

package me.nullaqua.api.kotlin

import me.nullaqua.api.reflect.FieldAccessor
import me.nullaqua.api.reflect.MethodAccessor
import me.nullaqua.api.reflect.ReflectAccessor
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@Throws(Throwable::class)
fun <T> T?.getField(fieldName: String): Any?
{
    val t = this ?: return null
    val clazz = t::class.java
    val field = FieldAccessor.getDeclaredField(clazz, fieldName)
    return field?.get(t)
}

@Throws(Throwable::class)
fun <T> T?.setField(fieldName: String, value: Any?)
{
    val t = this ?: return
    val clazz = t::class.java
    val field = FieldAccessor.getDeclaredField(clazz, fieldName)
    field?.set(t, value)
}

@Throws(Throwable::class)
fun <T> T?.invokeMethod(methodName: String, types: Array<Class<*>>, vararg args: Any?): Any?
{
    val t = this ?: return null
    val clazz = t::class.java
    val method = MethodAccessor.getDeclaredMethod(clazz, methodName, *types)
    return method?.invoke(t, *args)
}

@Throws(Throwable::class)
fun <T> Class<T>.blankInstance(): T
{
    return ReflectAccessor.blankInstance(this)
}

@Throws(Throwable::class)
inline fun <reified T> blankInstance(): T
{
    return T::class.java.blankInstance()
}

@Throws(Throwable::class)
fun <T> T?.forceClone(): T?
{
    return ReflectAccessor.cloneObject(this)
}

fun <T> T?.getAllFields(): List<FieldAccessor>
{
    return FieldAccessor.getDeclaredFields(this)
}

fun <T> T?.getAllMethods(): List<MethodAccessor>
{
    return MethodAccessor.getDeclaredMethods(this)
}

@OptIn(ExperimentalContracts::class)
inline fun <R> Any.lock(block: () -> R): R
{
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return synchronized(this,block)
}

fun Any.asObject() = Object::class.java.cast(this)
@Throws(InterruptedException::class)
fun Any.wait() = this.asObject().wait()
@Throws(InterruptedException::class)
fun Any.wait(millis: Long) = this.asObject().wait(millis)
@Throws(InterruptedException::class)
fun Any.wait(millis: Long, nanos: Int) = this.asObject().wait(millis, nanos)
fun Any.notify() = this.asObject().notify()
fun Any.notifyAll() = this.asObject().notifyAll()
