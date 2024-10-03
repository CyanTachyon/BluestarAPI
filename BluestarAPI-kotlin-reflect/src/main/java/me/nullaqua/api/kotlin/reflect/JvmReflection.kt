@file:JvmName("JvmReflection")
@file:Suppress("NOTHING_TO_INLINE", "UNUSED")

package me.nullaqua.api.kotlin.reflect

import me.nullaqua.api.reflect.*
import me.nullaqua.api.reflect.UnsafeOperation.blankInstance
import java.lang.reflect.Constructor
import kotlin.reflect.KClass

/**
 * 获取一个对象某字段的值.
 * 注意: 一个类和其父类中可能有同名字段, 只会获取子类中的字段.
 * @param fieldName 字段名
 * @throws NoSuchFieldException 当字段不存在时抛出
 * @throws Throwable 当反射出现错误时抛出
 */
@Throws(Throwable::class)
inline fun <T: Any> T.getField(fieldName: String): Any?
{
    val clazz = this::class.java
    val field = FieldAccessor.getFieldInSuperClasses(clazz, fieldName)
                ?: throw NoSuchFieldException("Field $fieldName not found")
    return field.get(this)
}

/**
 * 设置一个对象某字段的值.
 * 注意: 一个类和其父类中可能有同名字段, 只会设置子类中的字段(即使类型不匹配).
 * @param fieldName 字段名
 * @param value 字段值
 * @throws NoSuchFieldException 当字段不存在时抛出
 */
@Throws(Throwable::class)
inline fun <T: Any> T.setField(fieldName: String, value: Any?)
{
    val clazz = this::class.java
    val field = FieldAccessor.getFieldInSuperClasses(clazz, fieldName)
                ?: throw NoSuchFieldException("Field $fieldName not found")
    field.set(this, value)
}

@Throws(Throwable::class)
inline fun <T: Any> T.setField(fieldName: String, value: Any?, type: Class<in T>)
{
    val field = FieldAccessor.getField(type, fieldName)
                ?: throw NoSuchFieldException("Field $fieldName not found")
    field.set(this, value)
}

@Throws(Throwable::class)
inline fun <T: Any> T.invokeMethod(methodName: String, types: Array<Class<*>>, vararg args: Any?): Any?
{
    val clazz = this.javaClass
    val method = MethodAccessor.getMethodInSuperClasses(clazz, methodName, *types)
                 ?: throw NoSuchMethodException("Method $methodName not found")
    return method.invokeMethod(this, *args)
}

/**
 * 创建一个指定类型的空白实例, 空白实例指创建该实例时不会调用任何构造函数,
 * 这会使得该实例中所有基础类型均为默认值(0/false), 引用类型均为null.
 */
@Throws(Throwable::class)
@UnsafeJvmReflection
inline fun <T> Class<T>.blankInstance(): T = blankInstance(this)

/**
 * 创建一个指定类型的空白实例, 空白实例指创建该实例时不会调用任何构造函数,
 * 这会使得该实例中所有基础类型均为默认值(0/false), 引用类型均为null.
 */
@Throws(Throwable::class)
@UnsafeJvmReflection
inline fun <T: Any> KClass<T>.blankInstance(): T = java.blankInstance()

/**
 * 创建一个指定类型的空白实例
 * @throws Throwable 当反射出现错误时抛出
 */
@Throws(Throwable::class)
@UnsafeJvmReflection
inline fun <reified T: Any> blankInstance(): T = (T::class).blankInstance()

@Throws(Throwable::class)
@UnsafeJvmReflection
inline fun Any?.eraseToBlank() = UnsafeOperation.eraseToBlank(this)

/**
 * 返回一个[Void]的实例
 */
@Throws(Throwable::class)
@UnsafeJvmReflection
inline fun jvmVoidInstance(): Void = UnsafeOperation.voidInstance()

/**
 * 返回一个[System]的实例
 */
@Throws(Throwable::class)
@UnsafeJvmReflection
inline fun jvmSystemInstance(): System = blankInstance()

/**
 * 正常时返回nothing的实例，由于为了避免抛出错误，函数返回类型为[Any]但实际上为[Nothing].
 * 一般情况下nothing对应的java类为[Void], 也就是说一般情况下该方法的效果与[jvmVoidInstance]相同.
 * @throws Throwable 当反射出现错误时抛出
 */
@Throws(Throwable::class)
@UnsafeJvmReflection
inline fun nothingInstance(): Any = (Nothing::class as KClass<*>).blankInstance()

/**
 * 强制深克隆一个对象, 对目标类没有任何要求, 不需要实现Cloneable接口/Serializable接口/无参构造器.
 * 实现方式是
 */
@Throws(Throwable::class)
@UnsafeJvmReflection
inline fun <T> T?.forceDeepClone(): T? = UnsafeOperation.forceDeepObject(this)
inline fun <T> T?.getFieldsInSuperClasses(): List<FieldAccessor> =
    FieldAccessor.getFieldsInSuperClasses(this)

inline fun <T> T?.getMethodsInSuperClasses(): List<MethodAccessor> =
    MethodAccessor.getMethodsInSuperClasses(this)

@CallerSensitive
@KallerSensitive
inline fun getCaller(): StackWalker.StackFrame? = ReflectionAccessor.getCaller()

@CallerSensitive
@KallerSensitive
inline fun getCallerClass(): Class<*>? = ReflectionAccessor.getCallerClass()

@CallerSensitive
@KallerSensitive
inline fun getCallerMethod(): Invoker<*>? = ReflectionAccessor.getCallerMethod()

@CallerSensitive
@KallerSensitive
inline fun getCallers(): List<StackWalker.StackFrame> = ReflectionAccessor.getCallers()

@CallerSensitive
@KallerSensitive
inline fun getCallerClasses(): List<Class<*>> = ReflectionAccessor.getCallerClasses()

@CallerSensitive
@KallerSensitive
inline fun getCallerMethods(): List<Invoker<*>> = ReflectionAccessor.getCallerMethods()

/**
 * 在一个已经创建的对象上调用其构造函数, 通过该方法可以使得构造函数在一个对象上反复调用.
 */
@UnsafeJvmReflection
inline fun <T> T.invokeInitMethod(constructor: Constructor<out T>, vararg args: Any?)
{
    UnsafeOperation.getInitMethod(constructor).invokeMethod(this, *args)
}

/**
 * 表示该方法是不安全的, 可能会引发安全问题.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "This API is unsafe and may cause security issues.")
annotation class UnsafeJvmReflection

/**
 * 表示该函数对调用者敏感
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "This API is sensitive to the caller.")
annotation class KallerSensitive