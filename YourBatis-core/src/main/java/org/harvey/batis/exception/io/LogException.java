package org.harvey.batis.exception.io;

import org.harvey.batis.exception.PersistenceException;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-01 14:16
 */
public class LogException extends PersistenceException {
    public LogException(String message, Throwable cause) {
        super(message, cause);
    }
}
