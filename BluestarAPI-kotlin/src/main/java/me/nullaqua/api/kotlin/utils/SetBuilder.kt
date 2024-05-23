package me.nullaqua.api.kotlin.utils

import java.util.Collections

fun <T> buildSet(builder: SetBuilder<T>.()->Unit): Set<T> = SetBuilder<T>().apply(builder).build()
class SetBuilder<T> internal constructor()
{
    private val set = mutableSetOf<T>()
    operator fun T.unaryPlus() = set.add(this)
    fun build(): Set<T> = Collections.unmodifiableSet(set)
}