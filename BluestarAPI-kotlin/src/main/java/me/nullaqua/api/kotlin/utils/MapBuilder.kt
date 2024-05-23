package me.nullaqua.api.kotlin.utils

import java.util.Collections

fun <K, V> buildMap(builder: MapBuilder<K, V>.()->Unit): Map<K, V> = MapBuilder<K, V>().apply(builder).build()
class MapBuilder<K, V> internal constructor()
{
    private val map = mutableMapOf<K, V>()
    operator fun K.invoke(value: V)
    {
        map[this] = value
    }

    fun build(): Map<K, V> = Collections.unmodifiableMap(map)
}

fun <K> buildStringMap(builder: TreeMapBuilder<K>.()->Unit): Map<K, Any> = TreeMapBuilder<K>().apply(builder).build()
class TreeMapBuilder<K> internal constructor()
{
    private val map = mutableMapOf<K, Any>()
    operator fun K.invoke(value: Any)
    {
        map[this] = value
    }

    operator fun K.invoke(builder: TreeMapBuilder<K>.()->Unit)
    {
        map[this] = TreeMapBuilder<K>().apply(builder).build()
    }

    fun build(): Map<K, Any> = Collections.unmodifiableMap(map)
}