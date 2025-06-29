package me.nullaqua.api.kotlin.reflect

import me.nullaqua.api.reflect.FieldAccessor

class Fields(val obj: Any)
{
    private val fields = FieldAccessor.getFields(obj::class.java).associateBy { it.field.name }
    operator fun get(fieldName: String) = fields[fieldName]?.get(obj)
    operator fun set(fieldName: String, value: Any?) = fields[fieldName]?.set(obj, value)
    fun contains(fieldName: String) = fields.containsKey(fieldName)
    fun keys() = fields.keys
}