package org.harvey.batis.demo.exc;

/**
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-21 15:27
 */
public class NameExistException extends Exception {
    public NameExistException(String name) {
        super("Name " + name + " has existed");
    }

    public NameExistException() {
        super();
    }
}