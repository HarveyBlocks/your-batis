package org.harvey.batis.reflection;

/**
 * 反射器工厂接口
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-24 13:15
 */
public interface ReflectorFactory {
    boolean isClassCacheEnabled();

    void setClassCacheEnabled(boolean classCacheEnabled);

    /**
     * 映射器的创建方法
     */
    Reflector findForClass(Class<?> type);
}
