package me.lanzhi.api.reflect.classedit;

import javassist.CannotCompileException;
import javassist.CtMethod;

public class MethodBuilder
{
    private final CtMethod method;

    public MethodBuilder(CtMethod method)
    {
        this.method=method;
    }

    public MethodBuilder name(String name)
    {
        method.setName(name);
        return this;
    }

    public String name()
    {
        return method.getName();
    }

    public int modifiers()
    {
        return method.getModifiers();
    }

    public MethodBuilder modifiers(int modifiers)
    {
        method.setModifiers(modifiers);
        return this;
    }

    public MethodBuilder addModifier(int modifier)
    {
        method.setModifiers(method.getModifiers()|modifier);
        return this;
    }

    public MethodBuilder removeModifier(int modifier)
    {
        method.setModifiers(method.getModifiers()&~modifier);
        return this;
    }

    public MethodBuilder body(String body) throws CannotCompileException
    {
        method.setBody(body);
        return this;
    }

    public CtMethod method()
    {
        return method;
    }
}
