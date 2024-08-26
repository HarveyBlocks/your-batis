package org.harvey.batis.exception.binding;

import org.harvey.batis.exception.PersistenceException;

/**
 * 绑定MapperXML和Mapper接口
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-02 13:33
 */
public class BindingException extends PersistenceException {

    public BindingException() {
        super();
    }

    public BindingException(String message) {
        super(message);
    }

    public BindingException(String message, Throwable cause) {
        super(message, cause);
    }

    public BindingException(Throwable cause) {
        super(cause);
    }
}

