package org.harvey.batis.plugin;

import java.util.Properties;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-19 22:44
 */
public interface Interceptor {


    /**
     * 增强一个方法的具体逻辑
     *
     * @param invocation {@link Invocation}
     * @return 增强的方法的方法返回值
     * @throws Throwable 增强的方法的方法会抛出的异常
     */
    Object intercept(Invocation invocation) throws Throwable;

    /**
     * 增强一个类
     *
     * @param target 需要被增强的类
     * @return 增强后的代理类
     * @see Plugin#wrap(Object, Interceptor)
     */
    default Object plugin(Object target) {
        // 默认用本类进行增强
        return Plugin.wrap(target, this);
    }

    default void setProperties(Properties properties) {
        // NOP
    }

}
