package org.harvey.batis.reflection.wrapper;

import org.harvey.batis.reflection.MetaObject;

/**
 * ObjectWrapper产品的工厂类
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-24 11:18
 */
public interface ObjectWrapperFactory {

    default boolean hasWrapped(Object object) {
        return true;
    }

    ObjectWrapper wrap(MetaObject metaObject, Object object);

}

