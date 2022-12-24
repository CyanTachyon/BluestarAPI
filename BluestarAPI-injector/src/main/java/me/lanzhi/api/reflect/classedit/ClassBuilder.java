package me.lanzhi.api.reflect.classedit;

import javassist.*;
import me.lanzhi.api.reflect.MethodAccessor;

import java.security.ProtectionDomain;

public class ClassBuilder
{
    private final static MethodAccessor defineClassMethod=MethodAccessor.getMethod(ClassLoader.class,
                                                                                   "defineClass",
                                                                                   String.class,
                                                                                   byte[].class,
                                                                                   int.class,
                                                                                   int.class,
                                                                                   ProtectionDomain.class);
    private final CtClass ctClass;

    public ClassBuilder(ClassType type,String className)
    {
        final var pool=ClassPool.getDefault();
        switch (type)
        {
            case INTERFACE:
            {
                ctClass=pool.makeInterface(className);
                break;
            }
            case ANNOTATION:
            {
                ctClass=pool.makeAnnotation(className);
                break;
            }
            default:
            case TYPE:
            {
                ctClass=pool.makeClass(className);
            }
        }
    }

    public static ClassBuilder create(ClassType type,String className)
    {
        return new ClassBuilder(type,className);
    }

    public ClassBuilder modifier(int modifier)
    {
        ctClass.setModifiers(modifier);
        return this;
    }

    public int modifier()
    {
        return ctClass.getModifiers();

    }

    public ClassBuilder addModifier(int modifier)
    {
        ctClass.setModifiers(ctClass.getModifiers()|modifier);
        return this;
    }

    public ClassBuilder removeModifier(int modifier)
    {
        ctClass.setModifiers(ctClass.getModifiers()&~modifier);
        return this;
    }

    public ClassBuilder superClass(Class<?> superClass) throws CannotCompileException
    {
        ctClass.setSuperclass(ClassPool.getDefault().getOrNull(superClass.getName()));
        return this;
    }

    public ClassBuilder addImplement(Class<?>... interfaces) throws CannotCompileException
    {
        for (var i: interfaces)
        {
            ctClass.addInterface(ClassPool.getDefault().getOrNull(i.getName()));
        }
        return this;
    }

    public ClassBuilder implement(Class<?>... interfaces) throws CannotCompileException
    {
        ctClass.setInterfaces(toCtClass(interfaces));
        return this;
    }

    protected static CtClass[] toCtClass(Class<?>... parameters)
    {
        CtClass[] ctClasses=new CtClass[parameters.length];
        for (int i=0;i<parameters.length;i++)
        {
            ctClasses[i]=toCtClass(parameters[i]);
        }
        return ctClasses;
    }

    protected static CtClass toCtClass(Class<?> type)
    {
        if (type==null||type==void.class)
        {
            return CtClass.voidType;
        }
        CtClass ctClass=ClassPool.getDefault().getOrNull(type.getName());
        if (ctClass==null)
        {
            ClassPool.getDefault().appendClassPath(new ClassClassPath(type));
            ctClass=ClassPool.getDefault().getOrNull(type.getName());
        }
        return ctClass;
    }

    public ClassBuilder clearImplement()
    {
        ctClass.setInterfaces(new CtClass[0]);
        return this;
    }

    public ConstructorBuilder createConstructor(Class<?>... parameters)
    {
        CtClass[] ctClasses=toCtClass(parameters);
        try
        {
            CtConstructor ctConstructor=new CtConstructor(ctClasses,ctClass);
            ctClass.addConstructor(ctConstructor);
            return new ConstructorBuilder(ctConstructor);
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    public FieldBuilder createField(String name,Class<?> type) throws CannotCompileException
    {
        if (type==null||type==void.class)
        {
            throw new IllegalArgumentException("type cannot be null or void");
        }
        CtClass ctClass1=toCtClass(type);
        CtField ctField=new CtField(ctClass1,name,ctClass);
        ctClass.addField(ctField);
        return new FieldBuilder(ctField);
    }

    public MethodBuilder createMethod(Class<?> retuenType,String name,Class<?>... parameterTypes)
    {
        return createMethod(retuenType,name,false,parameterTypes);
    }

    public MethodBuilder createMethod(Class<?> returnType,String name,boolean defaultCode,Class<?>... parameters)
    {
        var ctClasses=toCtClass(parameters);
        try
        {
            var pool=ClassPool.getDefault();
            CtClass ctClass1=returnType!=null?pool.getCtClass(returnType.getName()):CtClass.voidType;
            CtMethod ctMethod=new CtMethod(ctClass1,name,ctClasses,ctClass);
            if (returnType!=null&&defaultCode)
            {
                ctMethod.setBody("return null;");
            }
            else if (defaultCode)
            {
                ctMethod.setBody(";");
            }
            ctClass.addMethod(ctMethod);
            return new MethodBuilder(ctMethod);
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    public MethodBuilder createMethod(Class<?> retuenType,String name,String body,Class<?>... parameterTypes) throws CannotCompileException
    {
        return createMethod(retuenType,name,true,parameterTypes).body(body);
    }

    public MethodBuilder createMethodDefault(Class<?> retuenType,String name,Class<?>... parameterTypes)
    {
        return createMethod(retuenType,name,true,parameterTypes);
    }

    public Class<?> build()
    {
        return build(this.getClass().getClassLoader());
    }

    public Class<?> build(ClassLoader loader)
    {
        try
        {
            var code=ctClass.toBytecode();
            return (Class<?>) MethodAccessor.getMethod(ClassLoader.class,
                                                       "defineClass",
                                                       String.class,
                                                       byte[].class,
                                                       int.class,
                                                       int.class,
                                                       ProtectionDomain.class)
                                            .invoke(loader,ctClass.getName(),code,0,code.length,null);
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    public enum ClassType
    {
        TYPE,
        INTERFACE,
        ANNOTATION
    }
}
