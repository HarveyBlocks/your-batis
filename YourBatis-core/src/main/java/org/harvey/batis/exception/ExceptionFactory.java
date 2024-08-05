package org.harvey.batis.exception;

import org.harvey.batis.util.ErrorContext;

/**
 * 将当前线程的{@link ErrorContext}的信息存入{@link PersistenceException}的message字段中, 然后返回
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-04 18:13
 */
public class ExceptionFactory {

    private ExceptionFactory() {
        // Prevent Instantiation
    }

    public static RuntimeException wrapException(String message, Exception e) {
        return new PersistenceException(ErrorContext.instance().setMessage(message).setCause(e).toString(), e);
    }

}
