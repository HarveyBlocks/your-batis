package org.harvey.batis.plugin;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 一个能被执行的方法代理
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-19 22:48
 */
@Getter
@AllArgsConstructor
public class Invocation {

    private final Object target;
    private final Method method;
    private final Object[] args;

    /**
     * 以反射的方式执行代理<br>
     * {@link #method}.invoke({@link #target}, {@link #args});
     *
     * @see Method#invoke(Object, Object...)
     */
    public Object proceed() throws InvocationTargetException, IllegalAccessException {
        return method.invoke(target, args);
    }

}

