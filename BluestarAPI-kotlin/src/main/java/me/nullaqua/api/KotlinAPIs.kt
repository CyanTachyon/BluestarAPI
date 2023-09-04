@file:JvmName("KotlinAPIs")

package me.nullaqua.api

import me.lanzhi.api.reflect.FieldAccessor
import me.lanzhi.api.reflect.MethodAccessor
import me.lanzhi.api.reflect.ReflectAccessor

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