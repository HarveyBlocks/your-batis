package org.harvey.batis.exception;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-25 08:45
 */
public abstract class YourbatisException extends RuntimeException {
    public YourbatisException() {
    }

    public YourbatisException(String message) {
        super(message);
    }

    public YourbatisException(String message, Throwable cause) {
        super(message, cause);
    }

    public YourbatisException(Throwable cause) {
        super(cause);
    }

    public YourbatisException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
