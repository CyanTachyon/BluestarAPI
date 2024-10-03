@file:Suppress("unused", "RemoveRedundantQualifierName")

package me.nullaqua.api.serializer

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import kotlin.experimental.or
import kotlin.experimental.xor

class DefaultSerializer: AbstractSerializer()
{
    companion object
    {
        val simpleCodes: Map<Class<*>, Byte> = mapOf(
            java.lang.Void.TYPE to 0x00,
            java.lang.Integer.TYPE to 0x01,
            java.lang.Long.TYPE to 0x02,
            java.lang.Short.TYPE to 0x03,
            java.lang.Byte.TYPE to 0x04,
            java.lang.Character.TYPE to 0x05,
            java.lang.Boolean.TYPE to 0x06,
            java.lang.Float.TYPE to 0x07,
            java.lang.Double.TYPE to 0x08,
            java.lang.Class::class.java to 0x09
        )
        const val SIMPLE_ARRAY_CODE: Byte = 0x10
        const val OBJECT_CODE = 0x20
        const val OBJECT_REFERENCE_CODE = 0x21
        fun getSimpleArrayCode(componentType: Class<*>): Byte = simpleCodes[componentType]!! or SIMPLE_ARRAY_CODE
    }

    private fun writeSimple(clazz: Class<*>, obj: Any?, out: DataOutputStream) = when (clazz)
    {
        java.lang.Integer.TYPE      -> out.writeInt(obj as Int)
        java.lang.Long.TYPE         -> out.writeLong(obj as Long)
        java.lang.Short.TYPE        -> out.writeShort((obj as Short).toInt())
        java.lang.Byte.TYPE         -> out.writeByte((obj as Byte).toInt())
        java.lang.Character.TYPE    -> out.writeChar((obj as Char).code)
        java.lang.Boolean.TYPE      -> out.writeBoolean(obj as Boolean)
        java.lang.Float.TYPE        -> out.writeFloat(obj as Float)
        java.lang.Double.TYPE       -> out.writeDouble(obj as Double)
        java.lang.Class::class.java -> out.writeUTF((obj as Class<*>).name)
        else                        -> Unit
    }

    private fun writeSimple(obj: Any?, out: DataOutputStream)
    {
        val clazz = (obj?.javaClass ?: Void.TYPE).let {
            if (it.isArray) it
            else simpleClasses[it] ?: error("Invalid code")
        }
        if (clazz.isArray)
        {
            out.writeByte(getSimpleArrayCode(clazz.componentType).toInt())
            val len = java.lang.reflect.Array.getLength(obj)
            val type = clazz.componentType
            out.writeInt(len)
            for (i in 0 until len)
                writeSimple(type, java.lang.reflect.Array.get(obj, i)!!, out)
        }
        else
        {
            out.writeByte(simpleCodes[clazz]!!.toInt())
            writeSimple(clazz, obj, out)
        }
    }

    private fun readSimpleObject(code: Byte, input: DataInputStream): Any?
    {
        return when (code)
        {
            0x00.toByte() -> null
            0x01.toByte() -> input.readInt()
            0x02.toByte() -> input.readLong()
            0x03.toByte() -> input.readShort()
            0x04.toByte() -> input.readByte()
            0x05.toByte() -> input.readChar()
            0x06.toByte() -> input.readBoolean()
            0x07.toByte() -> input.readFloat()
            0x08.toByte() -> input.readDouble()
            0x09.toByte() -> getClass(input.readUTF())
            else       -> error("Invalid code")
        }
    }

    private fun readSimple(code: Byte, inputStream: DataInputStream): Any?
    {
        if (code in simpleCodes.values) return readSimpleObject(code, inputStream)
        val code1 = code xor SIMPLE_ARRAY_CODE
        val len = inputStream.readInt()
        val array = java.lang.reflect.Array.newInstance(simpleCodes.keys.first { simpleCodes[it] == code1 }, len)
        for (i in 0 until len)
            java.lang.reflect.Array.set(array, i, readSimpleObject(code1, inputStream))
        return array
    }

    override fun serialize(obj: Any?, outputStream: OutputStream)
    {
        val out = DataOutputStream(outputStream)
        if (obj == null || obj.javaClass in simpleClasses || (obj.javaClass.isArray && obj !is Array<*>))
        {
            return writeSimple(obj, out)
        }
        val serialized = serialize(obj)
        out.writeByte(OBJECT_CODE)
        out.writeInt(serialized.size)
        for ((clazz, map) in serialized)
        {
            out.writeUTF(clazz.name)
            out.writeInt(map.size)
            for ((name, value) in map)
            {
                out.writeUTF(name)
                if (value is Object)
                {
                    out.writeByte(OBJECT_REFERENCE_CODE)
                    out.writeInt(value.index)
                }
                else writeSimple(value, out)
            }
        }
    }

    override fun deserialize(inputStream: InputStream): Any?
    {
        val input = DataInputStream(inputStream)
        val code = input.readByte()
        if (code in simpleCodes.values || (code xor SIMPLE_ARRAY_CODE) in simpleCodes.values)
            return readSimple(code, input)
        if (code != OBJECT_CODE.toByte())
            error("Invalid code")
        val size = input.readInt()
        val serialized: Array<Pair<Class<*>, Map<String, Any>>> = Array(size) { Any::class.java to emptyMap() }
        for (i in 0 until size)
        {
            val clazz = getClass(input.readUTF())
            val mapSize = input.readInt()
            val map = hashMapOf<String, Any>()
            for (j in 0 until mapSize)
            {
                val name = input.readUTF()
                val valueCode = input.readByte()
                if (valueCode == OBJECT_REFERENCE_CODE.toByte())
                {
                    val index = input.readInt()
                    map[name] = Object(index)
                }
                else if (valueCode in simpleCodes.values || (valueCode xor SIMPLE_ARRAY_CODE) in simpleCodes.values)
                {
                    val value = readSimple(valueCode, input) ?: continue
                    map[name] = value
                }
                else error("Invalid code")
            }
            serialized[i] = clazz to map
        }
        return deserialize(serialized)
    }
}