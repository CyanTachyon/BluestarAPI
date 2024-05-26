package me.nullaqua.api.serialize

import me.nullaqua.api.collection.Vector
import me.nullaqua.api.reflect.FieldAccessor
import me.nullaqua.kotlin.reflect.blankInstance
import me.nullaqua.kotlin.reflect.setField
import java.lang.reflect.Modifier
import java.util.*
import java.util.zip.Deflater
import java.util.zip.Inflater

object Serialize
{
    private val map = WeakHashMap<Any, SerializeObject>()

    @JvmStatic
    @Throws(Throwable::class)
    fun serialize(o: Any?): SerializeObject
    {
        val x = map[o]
        if (x != null)
        {
            return x
        }
        if (o == null) return SimpleObject(null)
        if (o.javaClass in SimpleObject.simpleClasses)
        {
            return SimpleObject(o)
        }
        if (o.javaClass.isArray)
        {
            val a = ArrayObject()
            map[o] = a
            a.init(o)
            return a
        }
        val a = ComplexObject()
        map[o] = a
        a.`init`(o)
        return a
    }

    @JvmStatic
    @Throws(Throwable::class)
    fun deserialize(o: SerializeObject): Any?
    {
        return o.deserialize(false)
    }

    @JvmStatic
    @Throws(Throwable::class)
    fun deserialize(sss: ByteArray): SerializeObject
    {
        //解压缩
        val inflater = Inflater()
        inflater.setInput(sss)
        val out = ByteArray(1024)
        val v = Vector<Byte>()
        while (!inflater.finished())
        {
            val len = inflater.inflate(out)
            for (i in 0 until len)
            {
                v.add(out[i])
            }
        }
        val s: ByteArray = v.toByteArray()
        val string = String(s)
        return deserialize(string)
    }

    @JvmStatic
    @Throws(Throwable::class)
    fun deserialize(sss: String): SerializeObject
    {
        val string = sss.replace("\t|\n|\r|\\s".toRegex(), "")
        if (!string.contains("{") && !string.contains("["))
        {
            val simple = SimpleObject::class.java.blankInstance()
            simple.deserialize(string)
            return simple
        }
        var i = 0
        val v = Vector<Pair<String, String>>()
        val res = Vector<SerializeObject>()
        while (true)
        {
            try
            {
                val (c, s, i2) = nxt(string, i)
                v.add(Pair(c, s))
                i = i2
            }
            catch (e: Throwable)
            {
                break
            }
        }
        for (p in v)
        {
            if (p.second.startsWith('{'))
            {
                res.add(ComplexObject())
            }
            else
            {
                res.add(ArrayObject())
            }
        }
        for (j in res.indices)
        {
            val x = res[j]
            if (x is ComplexObject)
            {
                x.from(v[j].first, v[j].second, res)
            }
            else
            {
                (x as ArrayObject).from(v[j].first, v[j].second, res)
            }
        }
        return res[0]
    }

    @JvmStatic
    @Throws(Throwable::class)
    private fun nxt(string: String, i: Int): Triple<String, String, Int>
    {
        var i = i
        val sb = StringBuilder()
        while (string[i] != ';' && string[i] != '{') sb.append(string[i++])
        if (string[i] == ';') sb.append(string[i++])
        val s = sb.toString()
        sb.clear()
        var x = 0
        while (true)
        {
            if (string[i] == '[' || string[i] == '{')
            {
                x++
            }
            else if (string[i] == ']' || string[i] == '}')
            {
                x--
                if (x == 0) break
            }
            sb.append(string[i])
            i++
        }
        sb.append(string[i++])
        val s2 = sb.toString()
        return Triple(s, s2, i)
    }
}

sealed class SerializeObject
{
    private var deserialize: Any? = null
    protected var t = Time(0, 0)
    abstract val serializeClass: Class<*>

    fun deserialize(new: Boolean): Any? = synchronized(this)
    {
        if (new || deserialize == null)
        {
            deserialize = deserialize(Time(System.currentTimeMillis(), System.nanoTime()))
        }
        else
        {
            deserialize
        }
        return@deserialize deserialize
    }

    fun deserialize(time: Time): Any? = synchronized(this)
    {
        return if (t.after(time)) deserialize
        else
        {
            t = time
            deserialize = blankInstance()
            deserializeTo(deserialize)
            deserialize
        }
    }

    override fun toString(): String
    {
        val map = HashMap<SerializeObject, Int>()
        val res = Vector<String>()
        toString(map, res)
        return res.joinToString("\n")
    }

    fun toBytes(): ByteArray
    {
        //toString之后压缩
        val bytes = toString().replace("\t|\n|\r|\\s".toRegex(), "").toByteArray(Charsets.UTF_8)
        //用java自带的Deflater和Inflater压缩和解压缩
        val deflater = Deflater()
        deflater.setInput(bytes)
        deflater.finish()
        val output = Vector<Byte>()
        val out = ByteArray(1024)
        while (!deflater.finished())
        {
            val len = deflater.deflate(out)
            for (i in 0 until len)
            {
                output.add(out[i])
            }
        }
        return output.toByteArray()
    }

    protected abstract fun deserializeTo(to: Any?)
    protected abstract fun blankInstance(): Any?
    abstract fun toString(map: MutableMap<SerializeObject, Int>, res: Vector<String>)
}

class SimpleObject(private val o: Any?) : SerializeObject()
{
    override val serializeClass: Class<*>

    init
    {
        val serializeClass: Class<*> = o?.javaClass ?: Void.TYPE
        require(simpleClasses.containsKey(serializeClass) || simpleClasses.containsValue(serializeClass)) { "Not a simple class" }
        if (simpleClasses.containsValue(serializeClass))
        {
            this.serializeClass = serializeClass
        }
        else
        {
            this.serializeClass = simpleClasses[serializeClass]!!
        }
    }

    public override fun deserializeTo(to: Any?)
    {
    }

    override fun blankInstance(): Any?
    {
        return o
    }

    override fun toString(map: MutableMap<SerializeObject, Int>, res: Vector<String>)
    {
    }

    override fun toString(): String
    {
        if (serializeClass == Void.TYPE) return "null"
        if (serializeClass == String::class.java)
        {
            val sb = StringBuilder()
            sb.append("String@")
            for (c in (o as java.lang.String).getBytes(Charsets.UTF_8))
            {
                //每个字节转为2个16进制字符
                sb.append(Integer.toHexString(c.toInt() shr 4 and 0xf))
                sb.append(Integer.toHexString(c.toInt() and 0xf))
            }
            return sb.toString()
        }
        if (serializeClass == Boolean::class.javaPrimitiveType)
        {
            return "boolean@" + (o as Boolean).toString()
        }
        if (serializeClass == Double::class.javaPrimitiveType)
        {
            return "double@" + java.lang.Double.doubleToRawLongBits(o as Double).toString(16)
        }
        if (serializeClass == Float::class.javaPrimitiveType)
        {
            return "float@" + java.lang.Float.floatToRawIntBits(o as Float).toString(16)
        }
        if (serializeClass == Char::class.javaPrimitiveType)
        {
            return "char@" + (o as Char).code.toString(16)
        }
        return serializeClass.name + "@" + (o as Number).toLong().toString(16)
    }

    fun deserialize(string: String)
    {
        if (string == "null")
        {
            this.setField("o", null)
            this.setField("serializeClass", Void.TYPE)
        }
        val i = string.indexOf('@')
        val s = string.substring(0, i)
        val x = string.substring(i + 1)
        val o = when (s)
        {
            "String" ->
            {
                val sb = StringBuilder()
                for (t in x.indices step 2)
                {
                    sb.append(Integer.parseInt(x.substring(t, t + 2), 16).toChar())
                }
                sb.toString()
            }

            "boolean" -> x.toBoolean()
            "double" -> java.lang.Double.longBitsToDouble(java.lang.Long.parseLong(x, 16))
            "float" -> java.lang.Float.intBitsToFloat(Integer.parseInt(x, 16))
            "char" -> x.toInt(16).toChar()
            "int" -> java.lang.Long.parseLong(x, 16).toInt()
            "long" -> java.lang.Long.parseLong(x, 16)
            "short" -> java.lang.Long.parseLong(x, 16).toShort()
            "byte" -> java.lang.Long.parseLong(x, 16).toByte()
            else -> throw IllegalArgumentException("Unknown type")
        }
        this.setField("o", o)
        this.setField("serializeClass", if (s == "String") java.lang.String::class.java else o::class.javaPrimitiveType)
    }

    //令伴类在编译后不存在,其中的内容作为SimpleObject的静态成员
    companion object
    {
        @JvmField
        val simpleClasses = mapOf(
            java.lang.String::class.java to java.lang.String::class.java,
            java.lang.Integer::class.java to Int::class.javaPrimitiveType,
            java.lang.Long::class.java to Long::class.javaPrimitiveType,
            java.lang.Short::class.java to Short::class.javaPrimitiveType,
            java.lang.Byte::class.java to Byte::class.javaPrimitiveType,
            java.lang.Character::class.java to Char::class.javaPrimitiveType,
            java.lang.Boolean::class.java to Boolean::class.javaPrimitiveType,
            java.lang.Float::class.java to Float::class.javaPrimitiveType,
            java.lang.Double::class.java to Double::class.javaPrimitiveType,
            java.lang.Void::class.java to Void::class.javaPrimitiveType
        )


    }
}

class ArrayObject : SerializeObject()
{
    private val array: Array<SerializeObject> = emptyArray()
    override val serializeClass: Class<*> = Array<Any>::class.java

    override fun deserializeTo(to: Any?)
    {
        if (to == null) throw NullPointerException()
        if (to.javaClass.componentType != serializeClass.componentType) throw IllegalArgumentException("Type mismatch")
        if (java.lang.reflect.Array.getLength(to) != array.size) throw IllegalArgumentException("Array length mismatch")
        for (i in array.indices)
        {
            java.lang.reflect.Array.set(to, i, array[i].deserialize(this.t))
        }
    }

    override fun blankInstance(): Any?
    {
        return java.lang.reflect.Array.newInstance(serializeClass.componentType, array.size)
    }

    override fun toString(map: MutableMap<SerializeObject, Int>, res: Vector<String>)
    {
        if (this in map) return
        val x = map.size
        map[this] = x
        val sb = StringBuilder()
        sb.append("${serializeClass.name}[")
        for (i in array.indices)
        {
            if (i != 0) sb.append(",")
            if (array[i] is SimpleObject)
            {
                sb.append(array[i].toString())
            }
            else
            {
                array[i].toString(map, res)
                sb.append(map[array[i]])
            }
        }
        sb.append("]")
        res[x] = sb.toString()
    }

    fun `init`(o: Any)
    {
        if (!o.javaClass.isArray) throw IllegalArgumentException("Type mismatch")
        this.setField("serializeClass", o::class.java)
        val array = Array(java.lang.reflect.Array.getLength(o)) {
            Serialize.serialize(java.lang.reflect.Array.get(o, it))
        }
        this.setField("array", array)
    }

    @Throws(Throwable::class)
    fun from(clazz: String, s: String, res: Vector<SerializeObject>)
    {
        val list = s.substring(1, s.length - 1).split(',')
        this.setField("serializeClass", Class.forName(clazz))
        this.setField("array", Array<SerializeObject?>(list.size) { null })
        for (j in list.indices)
        {
            val s = list[j]
            if (s[0] in '0'..'9')
            {
                val x = s.toInt()
                array[j] = res[x]
            }
            else
            {
                val x = SimpleObject::class.java.blankInstance()
                x.deserialize(s)
                array[j] = x
            }
        }
    }
}

class ComplexObject : SerializeObject()
{
    private val map: Map<Pair<String, String>, SerializeObject> = Collections.emptyMap()
    override val serializeClass: Class<*> = Any::class.java

    fun `init`(o: Any)
    {
        val map = HashMap<Pair<String, String>, SerializeObject>()
        val f = FieldAccessor.getFieldsInSuperClasses(o::class.java)
        for (i in f)
        {
            if (i.field.modifiers and Modifier.STATIC != 0) continue
            //所属类+字段名=序列化对象
            map[i.field.declaringClass.name to i.field.name] = Serialize.serialize(i[o])
        }
        //改为不可变map
        this.setField("map", java.util.Map.copyOf(map))
        this.setField("serializeClass", o::class.java)
    }

    public override fun deserializeTo(to: Any?)
    {
        if (to == null) throw NullPointerException()
        if (to::class.java != this.serializeClass) throw IllegalArgumentException("Type mismatch")
        for (i in map)
        {
            val clazz = Class.forName(i.key.first)
            val field = FieldAccessor.getField(clazz, i.key.second)
            if (field == null) println("$clazz ${i.key.second}")
            field[to] = i.value.deserialize(this.t)
        }
    }

    override fun blankInstance(): Any?
    {
        return serializeClass.blankInstance()
    }

    override fun toString(map: MutableMap<SerializeObject, Int>, res: Vector<String>)
    {
        if (this in map) return
        val x = map.size
        map[this] = x
        val sb = StringBuilder()
        sb.append("${serializeClass.name}{\n")
        val entrys = this.map.entries.toMutableList()
        entrys.sortBy { it.key.first }
        var last: Class<*>? = null
        for (i in entrys)
        {
            if (last != null && i.key.first != last.name) if (last != serializeClass) sb.append("\n\t}")
            if (last != null) sb.append(",\n")
            if (last == null || i.key.first != last.name)
            {
                last = Class.forName(i.key.first)
                if (last != serializeClass) sb.append("\t${last.name}{\n")
            }
            if (last != serializeClass) sb.append("\t");
            sb.append("\t${i.key.second}=")
            if (i.value is SimpleObject) sb.append(i.value.toString())
            else
            {
                i.value.toString(map, res)
                sb.append(map[i.value])
            }
        }
        if (last != null && last != serializeClass) sb.append("\n\t}")
        sb.append("\n}")
        res[x] = sb.toString()
    }

    @Throws(Throwable::class)
    fun from(clazz: String, s: String, res: Vector<SerializeObject>)
    {
        this.setField("serializeClass", Class.forName(clazz))
        val map = HashMap<Pair<String, String>, SerializeObject>()
        var j = 1
        var clazz0 = clazz
        val sb = StringBuilder()
        while (j < s.length)
        {
            sb.clear()
            while (s[j] != '{' && s[j] != '=' && s[j] != '}')
            {
                sb.append(s[j])
                j++
            }
            if (s[j] == '}')
            {
                clazz0 = clazz
                j += 2
                continue
            }
            val name = sb.toString()
            if (s[j] == '{')
            {
                clazz0 = name
                j++
                continue
            }
            j++
            sb.clear()
            while (s[j] != ',' && s[j] != '}')
            {
                sb.append(s[j])
                j++
            }
            val value = sb.toString()
            if (value[0] in '0'..'9')
            {
                val x = value.toInt()
                map[clazz0 to name] = res[x]
            }
            else
            {
                val x = SimpleObject::class.java.blankInstance()
                x.deserialize(value)
                map[clazz0 to name] = x
            }
            while (j < s.length && s[j] == ',') j++
        }
        this.setField("map", java.util.Map.copyOf(map))
    }
}
