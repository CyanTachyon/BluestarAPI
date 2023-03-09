package me.lanzhi.api.util.function;

import java.util.function.Supplier;

public interface SupplierWithThrow<T>
{
    default <E extends Throwable> T get(Class<E> eClass) throws E
    {
        try
        {
            return get();
        }
        catch (Throwable throwable)
        {
            throw RunWithThrow.check(throwable,eClass);
        }
    }

    T get() throws Throwable;

    default Supplier<T> toSupplier()
    {
        return ()->get(RuntimeException.class);
    }
}