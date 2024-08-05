package org.harvey.batis.exception.reflection;

import org.harvey.batis.exception.PersistenceException;

/**
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-24 14:03
 */
public class ReflectionException extends PersistenceException {
    public ReflectionException(String msg) {
        super(msg);
    }

    public ReflectionException(String msg, Throwable e) {
        super(msg, e);
    }
}
