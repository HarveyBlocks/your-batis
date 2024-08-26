package org.harvey.batis.exception.type;

import org.harvey.batis.exception.PersistenceException;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-13 21:15
 */
public class TypeException extends PersistenceException {
    public TypeException() {
        super();
    }

    public TypeException(String message) {
        super(message);
    }

    public TypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public TypeException(Throwable cause) {
        super(cause);
    }
}
