package org.harvey.batis.demo.exc;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-21 15:27
 */
public class StringTooLongException extends Exception {
    public StringTooLongException() {
        super("String is too long");
    }

    public StringTooLongException(String string) {
        super("String " + string + " is too long");
    }

    public StringTooLongException(String string, int limit) {
        super("String " + string + " is longer than the limit: " + limit);
    }
}
