package me.nullaqua.api.reflect;

public interface Invoker<T>
{
    T invoke(Object... args) throws Throwable;
}