package org.harvey.batis.exception.builder;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-04 15:56
 */
public class IncompleteElementException extends BuilderException {

    public IncompleteElementException() {
        super();
    }

    public IncompleteElementException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncompleteElementException(String message) {
        super(message);
    }

    public IncompleteElementException(Throwable cause) {
        super(cause);
    }

}
