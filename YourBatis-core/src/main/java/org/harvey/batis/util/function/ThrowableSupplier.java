package org.harvey.batis.util.function;

/**
 * 可以抛出异常的Supplier函数式接口
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-25 09:25
 */
@FunctionalInterface
public interface ThrowableSupplier<T, EXC extends Exception> {
    T get() throws EXC;
}