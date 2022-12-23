package me.lanzhi.api.reflect.classedit;

import javassist.*;

public class ClassBuilder
{
    private final CtClass ctClass;

    public ClassBuilder(ClassType type,String className)
    {
        final var pool=ClassPool.getDefault();
        switch (type)
        {
            case INTERFACE:
            {
                ctClass=pool.makeInterface(className);
                return;
            }
            case ANNOTATION:
            {
                ctClass=pool.makeAnnotation(className);
                return;
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

    public FieldBuilder createField(String name,Class<?> type) throws CannotCompileException
    {
        CtClass ctClass1=toCtClass(type)[0];
        CtField ctField=new CtField(ctClass1,name,ctClass);
        ctClass.addField(ctField);
        return new FieldBuilder(ctField);
    }

    private static CtClass[] toCtClass(Class<?>... parameters)
    {
        CtClass[] ctClasses=new CtClass[parameters.length];
        ClassPool pool=ClassPool.getDefault();
        for (int i=0;i<parameters.length;i++)
        {
            Class<?> parameter=parameters[i];
            try
            {
                ctClasses[i]=pool.getCtClass(parameter.getName());
            }
            catch (NotFoundException e)
            {
                pool.insertClassPath(new LoaderClassPath(parameter.getClassLoader()));
                try
                {
                    ctClasses[i]=pool.getCtClass(parameter.getName());
                }
                catch (NotFoundException ex)
                {
                    throw new RuntimeException(ex);
                }
            }
        }
        return ctClasses;
    }

    public MethodBuilder createMethod(Class<?> returnType,String name,Class<?>... parameters)
    {
        var ctClasses=toCtClass(parameters);
        try
        {
            var pool=ClassPool.getDefault();
            CtClass ctClass1=pool.getCtClass(returnType.getName());
            CtMethod ctMethod=new CtMethod(ctClass1,name,ctClasses,ctClass);
            ctClass.addMethod(ctMethod);
            return new MethodBuilder(ctMethod);
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
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

    public Class<?> build(ClassLoader loader)
    {
        try
        {
            return ctClass.toClass(loader);
        }
        catch (CannotCompileException e)
        {
            throw new RuntimeException(e);
        }
    }

    public Class<?> build()
    {
        try
        {
            return ctClass.toClass();
        }
        catch (CannotCompileException e)
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
