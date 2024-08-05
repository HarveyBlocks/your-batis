package org.harvey.batis.exception;

/**
 * 未完成的操作的异常
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-01 14:08
 */
public class UnfinishedFunctionException extends YourbatisException {
    public UnfinishedFunctionException(Object... o) {
        super();
    }

    public UnfinishedFunctionException() {
        super();
    }

    public UnfinishedFunctionException(String message) {
        super(message);
    }

    public UnfinishedFunctionException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnfinishedFunctionException(Throwable cause) {
        super(cause);
    }

    public UnfinishedFunctionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
