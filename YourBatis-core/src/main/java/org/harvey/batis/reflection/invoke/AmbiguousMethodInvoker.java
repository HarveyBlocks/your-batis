package org.harvey.batis.reflection.invoke;

import org.harvey.batis.exception.reflection.ReflectionException;

import java.lang.reflect.Method;

/**
 * 不确定的, 模棱两可的方法, 可以存在, 但不可以被代理执行
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-24 15:34
 */
public class AmbiguousMethodInvoker extends MethodInvoker {
    private final String exceptionMessage;

    public AmbiguousMethodInvoker(Method method, String exceptionMessage) {
        super(method);
        this.exceptionMessage = exceptionMessage;
    }

    @Override
    public Object invoke(Object target, Object[] args) {
        throw new ReflectionException(exceptionMessage);
    }
}

