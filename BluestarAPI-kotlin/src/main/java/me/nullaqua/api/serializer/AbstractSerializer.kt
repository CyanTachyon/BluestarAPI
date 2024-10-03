@file:Suppress("RedundantQualifierName", "unused")

package me.nullaqua.api.serializer

import me.nullaqua.api.collection.Vector
import me.nullaqua.api.kotlin.reflect.UnsafeJvmReflection
import me.nullaqua.api.kotlin.reflect.blankInstance
import me.nullaqua.api.kotlin.reflect.getFieldsInSuperClasses
import me.nullaqua.api.reflect.FieldAccessor
import java.util.*

abstract class AbstractSerializer: Serializer
{
    /**
     * 递归遍历[obj]及其所有引用的对象, 将其索引到[vector]中, 并将其引用关系保存到[map]中
     *
     * 基本类型、基本类型的包装类型、基本类型数组、null、Class不会被索引, 但String会被索引
     */
    private fun index(obj: Any, vector: Vector<Any>, map: MutableMap<Any, Int>)
    {
        // 如果是基本类型、基本类型数组、null就忽略
        if (obj.javaClass in simpleClasses || (obj.javaClass.isArray && obj !is Array<*>)) return
        // 如果已经索引过就忽略
        if (obj in map) return
        // 放入索引
        map[obj] = vector.size
        vector.add(obj)
        // 如果是数组对每个元素进行索引
        if (obj.javaClass.isArray)
        {
            // 如果是Object数组对每个元素进行索引
            if (obj is Array<*>) // 这里应始终为true, 因为上面已经排除了基本类型数组
                for (element in obj)
                    if (element != null)
                        index(element, vector, map)
            return
        }
        // 遍历该对象的所有字段, 对每个非静态字段进行索引
        for (field in obj.getFieldsInSuperClasses().filterNot { it.isStaticField })
        {
            val value = field.get(obj)
            if (value != null) index(value, vector, map)
        }
    }

    /**
     * 将[obj]序列化为一个数组, 数组中的每个元素为一个Pair代表一个对象, 该Pair的第一个元素为对象的类, 第二个元素为一个Map,
     * 该Map的key为字段名, value为字段值, 字段值可能为基本类型、基本类型数组、null、Object
     *
     * 注意: 该方法不能处理基本类型、基本类型的包装类型、基本类型数组、null、Class
     */
    protected fun serialize(obj: Any, keepNull: Boolean = false): Array<Pair<Class<*>, Map<String, Any>>>
    {
        if (obj.javaClass.isPrimitive || (obj.javaClass.isArray && obj !is Array<*>))
            error("Cannot serialize null or simple classes")
        val vector = Vector<Any>()
        // map要按照地址判断相等, 而不是调用equals方法
        val map = IdentityHashMap<Any, Int>()
        index(obj, vector, map)
        val result: Array<Pair<Class<*>, Map<String, Any>>> = Array(vector.size) { Any::class.java to emptyMap() }
        for ((i, value) in vector.withIndex())
        {
            val objMap = hashMapOf<String, Any>()
            if (value.javaClass.isArray)
            {
                // 如果是基本类型数组不会被索引, 所以可以断言是Object数组
                value as Array<*>
                objMap["size"] = value.size
                for ((j, element) in value.withIndex())
                {
                    if (element == null)
                    {
                        if (keepNull) objMap[j.toString()] = NullObject
                    }
                    else if (element.javaClass in simpleClasses) objMap[j.toString()] = element
                    else if (element.javaClass.isArray && element !is Array<*>) objMap[j.toString()] = element
                    else objMap[j.toString()] = Object(map[element]!!)
                }
                result[i] = value.javaClass to objMap
                continue
            }
            for (field in value.getFieldsInSuperClasses().filterNot { it.isStaticField })
            {
                val name = "${field.field.declaringClass.name}.${field.field.name}"
                val x = field.get(value)
                if (x == null)
                {
                    if (keepNull) objMap[name] = NullObject
                }
                else if (x.javaClass in simpleClasses) objMap[name] = x
                else if (x.javaClass.isArray && x !is Array<*>) objMap[name] = x
                else objMap[name] = Object(map[x]!!)
            }
            result[i] = value.javaClass to objMap
        }
        return result
    }

    protected fun deserialize(serialized: Array<Pair<Class<*>, Map<String, Any>>>): Any
    {
        val vector = Vector<Any>()
        for ((clazz, map) in serialized)
        {
            if (clazz.isArray) vector.add(
                java.lang.reflect.Array.newInstance(
                    clazz.componentType,
                    map["size"]!! as Int
                )
            )
            else vector.add(@OptIn(UnsafeJvmReflection::class) clazz.blankInstance())
        }
        for ((i, v) in serialized.withIndex())
        {
            val (clazz, map) = v
            val obj = vector[i]
            @Suppress("UNCHECKED_CAST")
            if (clazz.isArray)
            {
                val array = obj as Array<Any?>
                for ((name, value) in map)
                {
                    if (name == "size") continue
                    val index = name.toInt()
                    if (value is Object)
                    {
//                        println("${array}: ${value.index}, ${vector[value.index]::class.java}")
                        array[index] = vector[value.index]
                    }
                    else array[index] = value
                }
                continue
            }
            for ((name, value) in map)
            {
                val fieldClass = Class.forName(name.substringBeforeLast('.'))
                val fieldName = name.substringAfterLast('.')
                val field = FieldAccessor.getField(fieldClass, fieldName)
                if (value is Object) field.set(obj, vector[value.index])
                else field.set(obj, value)
            }
        }
        return vector.first()
    }

    internal data class Object(val index: Int)
    internal data object NullObject

    companion object
    {
        @JvmField
        val simpleClasses: Map<Class<*>, Class<*>> = mapOf(
            java.lang.Class::class.java to java.lang.Class::class.java,
            java.lang.Integer::class.java to java.lang.Integer.TYPE,
            java.lang.Long::class.java to java.lang.Long.TYPE,
            java.lang.Short::class.java to java.lang.Short.TYPE,
            java.lang.Byte::class.java to java.lang.Byte.TYPE,
            java.lang.Character::class.java to java.lang.Character.TYPE,
            java.lang.Boolean::class.java to java.lang.Boolean.TYPE,
            java.lang.Float::class.java to java.lang.Float.TYPE,
            java.lang.Double::class.java to java.lang.Double.TYPE,
            java.lang.Void::class.java to java.lang.Void.TYPE,
            java.lang.Integer.TYPE to java.lang.Integer.TYPE,
            java.lang.Long.TYPE to java.lang.Long.TYPE,
            java.lang.Short.TYPE to java.lang.Short.TYPE,
            java.lang.Byte.TYPE to java.lang.Byte.TYPE,
            java.lang.Character.TYPE to java.lang.Character.TYPE,
            java.lang.Boolean.TYPE to java.lang.Boolean.TYPE,
            java.lang.Float.TYPE to java.lang.Float.TYPE,
            java.lang.Double.TYPE to java.lang.Double.TYPE,
            java.lang.Void.TYPE to java.lang.Void.TYPE,
        )

        @JvmStatic
        fun getClass(className: String): Class<*> = when (className)
        {
            "int"    -> java.lang.Integer.TYPE
            "long"   -> java.lang.Long.TYPE
            "short"  -> java.lang.Short.TYPE
            "byte"   -> java.lang.Byte.TYPE
            "char"   -> java.lang.Character.TYPE
            "boolean"-> java.lang.Boolean.TYPE
            "float"  -> java.lang.Float.TYPE
            "double" -> java.lang.Double.TYPE
            "void"   -> java.lang.Void.TYPE
            "class"  -> Class::class.java
            else     -> Class.forName(className)
        }
    }
}