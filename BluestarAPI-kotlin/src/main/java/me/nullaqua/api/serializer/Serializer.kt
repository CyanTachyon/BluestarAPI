package me.nullaqua.api.serializer

import java.io.InputStream
import java.io.OutputStream

interface Serializer
{
    fun serialize(obj: Any?, outputStream: OutputStream)
    fun deserialize(inputStream: InputStream): Any?
}