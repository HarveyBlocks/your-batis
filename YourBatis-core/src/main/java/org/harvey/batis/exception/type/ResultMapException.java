package org.harvey.batis.exception.type;

import org.harvey.batis.exception.PersistenceException;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-13 21:20
 */
public class ResultMapException extends PersistenceException {
    public ResultMapException() {
        super();
    }

    public ResultMapException(String message) {
        super(message);
    }

    public ResultMapException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResultMapException(Throwable cause) {
        super(cause);
    }
}
