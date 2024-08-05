package org.harvey.batis.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * 在反射过程中, 代理抛出的异常外会包裹一层异常, 本工具类去除这层异常包裹
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-25 19:53
 */
public class ReflectionExceptionUnwrappedMaker {
    private ReflectionExceptionUnwrappedMaker() {
    }

    public static Throwable unwrapThrowable(Throwable wrapped) {
        Throwable unwrapped = wrapped;
        while (true) {
            if (unwrapped instanceof InvocationTargetException) {
                // 例如Method类的对象method调用invoke, invoke在执行method的逻辑时发生异常
                // 异常栈中就会带有InvocationTargetException,然后再是异常本身
                unwrapped = ((InvocationTargetException) unwrapped).getTargetException();
            } else if (unwrapped instanceof UndeclaredThrowableException) {
                // 子类方法抛出了比父类方法定义更广泛的异常, 本现象引发的异常就是UndeclaredThrowableException
                // 一般编译器会检查这种情况
                // 但是在动态代理的情况下, 就会违反这种约束
                unwrapped = ((UndeclaredThrowableException) unwrapped).getUndeclaredThrowable();
            } else {
                return unwrapped;
            }
        }
    }
}
