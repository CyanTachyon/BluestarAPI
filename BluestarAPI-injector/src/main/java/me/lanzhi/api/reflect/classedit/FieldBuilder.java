package me.lanzhi.api.reflect.classedit;

import javassist.CtField;

public class FieldBuilder
{
    private final CtField field;

    protected FieldBuilder(CtField ctField)
    {
        this.field=ctField;
    }

    public FieldBuilder name(String name)
    {
        field.setName(name);
        return this;
    }

    public String name()
    {
        return field.getName();
    }

    public int modifiers()
    {
        return field.getModifiers();
    }

    public FieldBuilder modifiers(int modifiers)
    {
        field.setModifiers(modifiers);
        return this;
    }

    public FieldBuilder addModifier(int modifier)
    {
        field.setModifiers(field.getModifiers()|modifier);
        return this;
    }

    public FieldBuilder removeModifier(int modifier)
    {
        field.setModifiers(field.getModifiers()&~modifier);
        return this;
    }

    public CtField field()
    {
        return field;
    }
}
