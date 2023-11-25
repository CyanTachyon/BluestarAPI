package me.nullaqua.api.util.function;

import java.util.function.Consumer;

/**
 * 执行可能抛出异常的Consumer。
 *
 * @param <T> 参数类型
 */
public interface ConsumerWithThrow<T>
{
    /**
     * 将ConsumerWithThrow对象转换为Consumer对象。
     *
     * @return Consumer对象
     */
    default Consumer<T> toConsumer()
    {
        return t -> accept(t,RuntimeException.class);
    }

    /**
     * 接受一个参数并执行操作。
     *
     * @param t 参数
     * @throws Throwable 可能抛出的异常
     */
    void accept(T t) throws Throwable;

    /**
     * 只抛出指定类型的受检异常。其他异常将被包装为RuntimeException抛出。
     *
     * @param t      参数
     * @param eClass 异常的类
     * @throws E 可能抛出的异常
     */
    default <E extends Throwable> void accept(T t,Class<E> eClass) throws E
    {
        ((RunWithThrow) () -> accept(t)).run(eClass);
    }
}