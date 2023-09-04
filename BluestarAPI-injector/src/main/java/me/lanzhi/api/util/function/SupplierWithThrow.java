package me.lanzhi.api.util.function;

import java.util.function.Supplier;

/**
 * 一个可能会抛出异常的Supplier。
 */
public interface SupplierWithThrow<T>
{
    /**
     * 获取对象。并抛出指定类型的受检异常。其他异常将被包装为RuntimeException抛出。
     *
     * @param eClass 指定类型的受检异常
     * @param <E>    指定类型的受检异常
     * @return 对象
     * @throws E 指定类型的受检异常
     */
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

    /**
     * 获取对象，可能会抛出异常。
     *
     * @return 对象
     * @throws Throwable 异常
     */
    T get() throws Throwable;

    /**
     * 将SupplierWithThrow对象转换为Supplier对象。
     *
     * @return Supplier对象
     */
    default Supplier<T> toSupplier()
    {
        return () -> get(RuntimeException.class);
    }
}