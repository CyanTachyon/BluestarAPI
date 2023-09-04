package me.lanzhi.api

interface Coder<T, R> : Encoder<T, R>, Decoder<R, T>
{
}

interface Encoder<in T, out R>
{
    fun encode(t: T): R

    fun canEncodeClass(): Class<in T>
}

interface Decoder<in R, out T>
{
    fun decode(r: R): T

    fun canDecodeClass(): Class<in R>
}