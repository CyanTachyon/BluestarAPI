package me.lanzhi.api.util.function;

public interface ConsumerWithThrow<T>
{
    default <E extends Throwable> void accept(T t,Class<E> eClass) throws E
    {
        ((RunWithThrow)()->accept(t)).run(eClass);
    }

    void accept(T t) throws Throwable;
}
