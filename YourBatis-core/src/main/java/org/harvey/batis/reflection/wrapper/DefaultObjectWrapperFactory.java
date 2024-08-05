package org.harvey.batis.reflection.wrapper;

import org.harvey.batis.exception.reflection.ReflectionException;
import org.harvey.batis.reflection.MetaObject;

/**
 * 类型的包装工厂的默认实现
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-24 13:19
 */
public class DefaultObjectWrapperFactory implements ObjectWrapperFactory {
    @Override
    public boolean hasWrapped(Object object) {
        return false;
    }

    @Override
    public ObjectWrapper wrap(MetaObject metaObject, Object object) {
        // 默认包装工厂不能直接调用包装方法
        throw new ReflectionException("The DefaultObjectWrapperFactory should never be called to provide an ObjectWrapper.");
    }
}
