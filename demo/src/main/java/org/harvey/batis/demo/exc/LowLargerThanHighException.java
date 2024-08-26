package org.harvey.batis.demo.exc;

/**
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-21 15:27
 */
public class LowLargerThanHighException extends Exception {
    public LowLargerThanHighException() {
        super("Low Larger Than High");
    }

    public LowLargerThanHighException(double low, double high) {
        super("Low " + low + " Larger Than High " + high);
    }
}
