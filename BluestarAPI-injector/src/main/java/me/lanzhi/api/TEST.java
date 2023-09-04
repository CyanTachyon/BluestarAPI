package me.lanzhi.api;

import me.lanzhi.api.reflect.ReflectAccessor;

public class TEST
{
    public static void main(String[] args) throws Throwable
    {
        A a=ReflectAccessor.blankInstance(A.class);
        System.out.println(a);
    }
}

class A
{
    int a, b;

    public A()
    {
        a=1;
        b=2;
        System.out.println("构造函数被调用");
        throw new RuntimeException();
    }

    @Override
    public String toString()
    {
        return "{ a="+a+", b="+b+" }";
    }
}