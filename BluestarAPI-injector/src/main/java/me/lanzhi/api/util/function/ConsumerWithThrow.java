package me.lanzhi.api.util.function;

import java.util.function.Consumer;

public interface ConsumerWithThrow<T>
{
    default Consumer<T> toConsumer()
    {
        return t->accept(t,RuntimeException.class);
    }

    void accept(T t) throws Throwable;

    default <E extends Throwable> void accept(T t,Class<E> eClass) throws E
    {
        ((RunWithThrow) ()->accept(t)).run(eClass);
    }
}
