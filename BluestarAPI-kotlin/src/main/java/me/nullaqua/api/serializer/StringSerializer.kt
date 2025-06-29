@file:Suppress("KotlinConstantConditions", "unused", "RemoveRedundantQualifierName")

package me.nullaqua.api.serializer

import me.nullaqua.api.collection.Vector
import java.io.InputStream
import java.io.OutputStream
import java.io.Reader
import java.io.Writer
import java.nio.charset.Charset
import java.util.LinkedList

class StringSerializer(
    private val prettyPrint: Boolean = false,
    private val keepNull: Boolean = false,
    private val charset: Charset = Charsets.UTF_8
): AbstractSerializer()
{
    companion object
    {
        // 等于
        const val EQUAL = '='

        // 连接符
        const val CONNECTOR = '@'

        // 结束符
        const val TERMINATOR = ';'

        // 分隔符
        const val SEPARATOR = ','

        // 结构开始
        const val STRUCTURE_START = '{'

        // 结构结束
        const val STRUCTURE_END = '}'

        // 数组开始
        const val ARRAY_START = '['

        // 数组结束
        const val ARRAY_END = ']'

        // 转译符
        const val ESCAPE = '\\'
        private fun escape(char: Char): String = when (char)
        {
            ESCAPE          -> "$ESCAPE$ESCAPE"
            EQUAL           -> "$ESCAPE$EQUAL"
            CONNECTOR       -> "$ESCAPE$CONNECTOR"
            TERMINATOR      -> "$ESCAPE$TERMINATOR"
            SEPARATOR       -> "$ESCAPE$SEPARATOR"
            STRUCTURE_START -> "$ESCAPE$STRUCTURE_START"
            STRUCTURE_END   -> "$ESCAPE$STRUCTURE_END"
            else            ->
            {
                if (char.isWhitespace()) "${ESCAPE}u${char.code.toString(16).padStart(4, '0')}"
                else char.toString()
            }
        }

        private fun escape(string: String): String
        {
            val sb = StringBuilder()
            for (c in string) sb.append(escape(c))
            return sb.toString()
        }

        private fun unescape(string: String): String
        {
            val sb = StringBuilder()
            var b = false
            var i = 0
            while (i < string.length)
            {
                if (b)
                {
                    when (string[i])
                    {
                        ESCAPE          -> sb.append(ESCAPE)
                        EQUAL           -> sb.append(EQUAL)
                        CONNECTOR       -> sb.append(CONNECTOR)
                        TERMINATOR      -> sb.append(TERMINATOR)
                        SEPARATOR       -> sb.append(SEPARATOR)
                        STRUCTURE_START -> sb.append(STRUCTURE_START)
                        STRUCTURE_END   -> sb.append(STRUCTURE_END)
                        'u'             ->
                        {
                            val code = string.substring(i+1, i+5).toInt(16)
                            sb.append(code.toChar())
                            i += 4
                        }

                        else            -> error("Invalid escape character")
                    }
                    b = false
                }
                else if (string[i] == ESCAPE) b = true
                else sb.append(string[i])
                i++
            }
            return sb.toString()
        }
    }

    private fun Writer.writeSimpleObject(obj: Any?)
    {
        when (obj)
        {
            is Class<*> -> write(escape(obj.name))
            is Byte     -> write(obj.toUByte().toString(16))
            is Short    -> write(obj.toUShort().toString(16))
            is Int      -> write(obj.toUInt().toString(16))
            is Long     -> write(obj.toULong().toString(16))
            is Float    -> write(obj.toRawBits().toUInt().toString(16))
            is Double   -> write(obj.toRawBits().toULong().toString(16))
            is Char     -> write(obj.code.toUInt().toString(16))
            is Boolean  -> write(obj.toString())
            is Void     -> write("null")
            null        -> write("null")
            else        -> error("Invalid code")
        }
    }

    private fun Writer.writeSimple(obj: Any?)
    {
        if (obj != null && obj.javaClass.isArray && obj !is Array<*>)
        {
            write("${obj.javaClass.name}${CONNECTOR}${ARRAY_START}")
            val len = java.lang.reflect.Array.getLength(obj)
            for (i in 0 until len)
            {
                writeSimpleObject(java.lang.reflect.Array.get(obj, i)!!)
                if (i != len-1) write("$SEPARATOR")
            }
            write("$ARRAY_END")
        }
        else if (obj != null && obj.javaClass in simpleClasses)
        {
            write("${simpleClasses[obj.javaClass]!!.name}${CONNECTOR}")
            writeSimpleObject(obj)
        }
        else if (obj == null)
        {
            write("void${CONNECTOR}")
            writeSimpleObject(null)
        }
        else error("Invalid code")
    }

    override fun serialize(obj: Any?, outputStream: OutputStream)
    {
        val out = outputStream.bufferedWriter(charset)
        if (obj == null || obj.javaClass in simpleClasses || (obj.javaClass.isArray && obj !is Array<*>))
        {
            out.writeSimple(obj)
            out.write("$TERMINATOR")
            out.flush()
            return
        }
        val serialized = serialize(obj, keepNull = keepNull)
        for ((index, v) in serialized.withIndex())
        {
            val (clazz, map) = v
            out.write(escape(clazz.name))
            out.write("$CONNECTOR$STRUCTURE_START")
            if (prettyPrint) out.newLine()
            for ((name, value) in map)
            {
                if (prettyPrint) out.write("  ")
                out.write(escape(name))
                out.write("$EQUAL")
                when (value)
                {
                    is Object  -> out.write("Object${CONNECTOR}${value.index.toString(16)}")
                    NullObject -> out.writeSimple(null)
                    else       -> out.writeSimple(value)
                }
                out.write("$SEPARATOR")
                if (prettyPrint) out.write("\n")
            }
            out.write("$STRUCTURE_END")
            if (index == serialized.size-1)
                out.write("$TERMINATOR")
            else
                out.write("$SEPARATOR")
            if (prettyPrint) out.write("\n")
        }
        out.flush()
    }

    private fun Reader.nextChar(): Char?
    {
        while (true)
        {
            val char = this.read()
            if (char == -1) return null
            if (!char.toChar().isWhitespace()) return char.toChar()
        }
    }

    private fun Reader.readUntil(vararg chars: Char): String
    {
        val sb = StringBuilder()
        var b = false
        var c = this.nextChar() ?: return ""
        while (c !in chars || b)
        {
            sb.append(c)
            if (b) b = false
            else if (c == ESCAPE) b = true
            c = this.nextChar() ?: break
        }
        return sb.toString()
    }

    private fun readSimple(clazz: Class<*>, value: String): Any? =
        when (clazz)
        {
            java.lang.Byte.TYPE         -> value.toUByte(16).toByte()
            java.lang.Short.TYPE        -> value.toUShort(16).toShort()
            java.lang.Integer.TYPE      -> value.toUInt(16).toInt()
            java.lang.Long.TYPE         -> value.toULong(16).toLong()
            java.lang.Float.TYPE        -> value.toUInt(16).toInt().let(Float::fromBits)
            java.lang.Double.TYPE       -> value.toULong(16).toLong().let(Double::fromBits)
            java.lang.Boolean.TYPE      -> value.toBooleanStrict()
            java.lang.Character.TYPE    -> unescape(value)[0]
            java.lang.Void.TYPE         -> null
            java.lang.Class::class.java -> getClass(unescape(value))
            else                        -> error("Invalid code")
        }

    private fun Reader.readSimpleArray(clazz: Class<*>): Any
    {
        readUntil(ARRAY_START)
        val array = LinkedList<Any?>()
        while (true)
        {
            val v = readUntil(SEPARATOR, ARRAY_END, TERMINATOR)
            if (v.isEmpty()) break
            array.add(readSimple(clazz.componentType, v))
        }
        val resArray = java.lang.reflect.Array.newInstance(clazz.componentType, array.size)
        for ((i, v) in array.withIndex()) java.lang.reflect.Array.set(resArray, i, v)
        return resArray
    }

    override fun deserialize(inputStream: InputStream): Any?
    {
        val reader = inputStream.bufferedReader(charset)
        val vector = Vector<Pair<Class<*>, Map<String, Any>>>()
        while (true)
        {
            val clazzName = unescape(reader.readUntil(CONNECTOR))
            val clazz = getClass(clazzName)
            if (clazz in simpleClasses)
                return readSimple(clazz, reader.readUntil(TERMINATOR))
            if (clazz.isArray && clazz.componentType.isPrimitive)
                return reader.readSimpleArray(clazz)
            val map = hashMapOf<String, Any>()
            reader.readUntil(STRUCTURE_START)
            while (true)
            {
                val name = unescape(reader.readUntil(EQUAL, STRUCTURE_END))
                if (name.isEmpty()) break
                val valueType = unescape(reader.readUntil(CONNECTOR))
                val value = if (valueType == "Object")
                    Object(readSimple(Integer.TYPE, reader.readUntil(SEPARATOR)) as Int)
                else
                {
                    val c = getClass(valueType)
                    if (c.isArray && c.componentType.isPrimitive)
                        reader.readSimpleArray(c)
                    else readSimple(getClass(valueType), reader.readUntil(SEPARATOR)) ?: continue
                }
                map[name] = value
            }
            vector.add(clazz to map)
            val c = reader.nextChar() ?: break
            if (c == TERMINATOR) break
        }
        val array = vector.toArray(Array<Pair<Class<*>, Map<String, Any>>>(vector.size) { Any::class.java to emptyMap() })
        return deserialize(array)
    }
}