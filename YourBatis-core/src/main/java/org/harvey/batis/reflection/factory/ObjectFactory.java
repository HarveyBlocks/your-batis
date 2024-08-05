package org.harvey.batis.reflection.factory;

import java.util.List;
import java.util.Properties;

/**
 * 接口
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-24 13:19
 */
public interface ObjectFactory {

    default void setProperties(Properties properties) {
        // NOP
    }

    /**
     * 用无参构造构建Object
     */
    <T> T create(Class<T> type);

    /**
     * 用指定构造器构造Object
     */
    <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs);

    /**
     * 如果此对象可以具有一组其他对象，则返回 true。<br>
     * <b>它的主要目的是支持非 java.util.Collection 对象</b>
     *
     * @param type 需要被检查的类型
     * @return type是否是Collection
     */
    public <T> boolean isCollection(Class<T> type);
}