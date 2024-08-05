package org.harvey.batis.reflection.invoke;

import lombok.Getter;
import org.harvey.batis.reflection.Reflector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 方法的代理, 这个方法一般就是Getter和Setter了
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-24 15:34
 */
@Getter
public class MethodInvoker implements Invoker {
    private final Method method;

    public MethodInvoker(Method method) {
        this.method = method;
    }

    @Override
    public Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException {
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException e) {
            if (Reflector.canControlMemberAccessible()) {
                method.setAccessible(true);
                return method.invoke(target, args);
            } else {
                throw e;
            }
        }
    }

    @Override
    public Class<?> getType() {
        return method.getParameterTypes().length == 1 ?
                method.getParameterTypes()[0] :
                method.getReturnType();
    }
}
