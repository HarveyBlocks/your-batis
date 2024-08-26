package org.harvey.batis.exception;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-12 23:22
 */
public class TooManyElementsException extends PersistenceException {
    public TooManyElementsException() {
        super();
    }

    public TooManyElementsException(String message) {
        super(message);
    }

    public TooManyElementsException(String message, Throwable cause) {
        super(message, cause);
    }

    public TooManyElementsException(Throwable cause) {
        super(cause);
    }
}
