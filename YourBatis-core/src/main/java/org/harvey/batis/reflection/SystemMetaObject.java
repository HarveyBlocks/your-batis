package org.harvey.batis.reflection;

import org.harvey.batis.reflection.factory.DefaultObjectFactory;
import org.harvey.batis.reflection.factory.ObjectFactory;
import org.harvey.batis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.harvey.batis.reflection.wrapper.ObjectWrapperFactory;


/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-24 13:17
 */
public final class SystemMetaObject {

    public static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
    public static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
    public static final MetaObject NULL_META_OBJECT =
            MetaObject.forObject(NullObject.class, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());

    private SystemMetaObject() {
        // Prevent Instantiation of Static Class
    }

    private static class NullObject {
    }

    /**
     * 将Object进行到MetaObject的封装
     */
    public static MetaObject forObject(Object object) {
        return MetaObject.forObject(
                object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY,
                new DefaultReflectorFactory());
    }

}