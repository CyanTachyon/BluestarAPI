package me.lanzhi.api.reflect.classedit;

import javassist.CannotCompileException;
import javassist.CtConstructor;

public class ConstructorBuilder
{
    private final CtConstructor constructor;

    public ConstructorBuilder(CtConstructor constructor)
    {
        this.constructor=constructor;
    }

    public ConstructorBuilder body(String body) throws CannotCompileException
    {
        constructor.setBody(body);
        return this;
    }

    public CtConstructor constructor()
    {
        return constructor;
    }

    public ConstructorBuilder modifiers(int modifiers)
    {
        constructor.setModifiers(modifiers);
        return this;
    }

    public ConstructorBuilder addModifier(int modifier)
    {
        constructor.setModifiers(constructor.getModifiers()|modifier);
        return this;
    }

    public ConstructorBuilder removeModifier(int modifier)
    {
        constructor.setModifiers(constructor.getModifiers()&~modifier);
        return this;
    }

    public int modifiers()
    {
        return constructor.getModifiers();
    }
}
