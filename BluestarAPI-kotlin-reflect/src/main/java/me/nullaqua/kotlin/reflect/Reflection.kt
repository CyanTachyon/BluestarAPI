@file:JvmName("KotlinReflection")
@file:Suppress("NOTHING_TO_INLINE", "UNUSED")

package me.nullaqua.kotlin.reflect

import me.nullaqua.api.reflect.CallerSensitive
import me.nullaqua.api.reflect.FieldAccessor
import me.nullaqua.api.reflect.Invoker
import me.nullaqua.api.reflect.MethodAccessor
import me.nullaqua.api.reflect.ReflectionAccessor
import java.lang.reflect.Executable
import kotlin.reflect.KClass

@Throws(Throwable::class)
inline fun <T> T?.getField(fieldName: String): Any?
{
    val t = this ?: return null
    val clazz = t::class.java
    val field = FieldAccessor.getDeclaredField(clazz, fieldName)
    return field?.get(t)
}

@Throws(Throwable::class)
inline fun <T> T?.setField(fieldName: String, value: Any?)
{
    val t = this ?: return
    val clazz = t::class.java
    val field = FieldAccessor.getDeclaredField(clazz, fieldName)
    field?.set(t, value)
}

@Throws(Throwable::class)
inline fun <T> T?.invokeMethod(methodName: String, types: Array<Class<*>>, vararg args: Any?): Any?
{
    val t = this ?: return null
    val clazz = t::class.java
    val method = MethodAccessor.getDeclaredMethod(clazz, methodName, *types)
    return method?.invokeMethod(t, *args)
}

@Throws(Throwable::class)
inline fun <T> Class<T>.blankInstance(): T = ReflectionAccessor.blankInstance(this)
@Throws(Throwable::class)
inline fun <T: Any> KClass<T>.blankInstance(): T = java.blankInstance()
@Throws(Throwable::class)
inline fun jvmVoidInstance(): Void = ReflectionAccessor.voidInstance()

/**
 * 正常时返回nothing的实例，由于为了避免抛出错误，函数返回类型为[Any]但实际上为[Nothing]
 * @throws Throwable 当反射出现错误时抛出
 */
@Throws(Throwable::class)
inline fun nothingInstance(): Any = (Nothing::class as KClass<*>).blankInstance()


@Throws(Throwable::class)
inline fun <reified T: Any> blankInstance(): T = (T::class).blankInstance()

@Throws(Throwable::class)
inline fun <T> T?.forceClone(): T?
{
    return ReflectionAccessor.cloneObject(this)
}

inline fun <T> T?.getAllFields(): List<FieldAccessor>
{
    return FieldAccessor.getDeclaredFields(this)
}

inline fun <T> T?.getAllMethods(): List<MethodAccessor>
{
    return MethodAccessor.getDeclaredMethods(this)
}

@CallerSensitive
inline fun getCaller(): StackWalker.StackFrame? = ReflectionAccessor.getCaller()
@CallerSensitive
inline fun getCallerClass(): Class<*>? = ReflectionAccessor.getCallerClass()
@CallerSensitive
inline fun getCallerMethod(): Invoker<*>? = ReflectionAccessor.getCallerMethod()
@CallerSensitive
inline fun getCallers(): List<StackWalker.StackFrame> = ReflectionAccessor.getCallers()
@CallerSensitive
inline fun getCallerClasses(): List<Class<*>> = ReflectionAccessor.getCallerClasses()
@CallerSensitive
inline fun getCallerMethods(): List<Invoker<*>> = ReflectionAccessor.getCallerMethods()