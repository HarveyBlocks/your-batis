package org.harvey.batis.demo.exc;

/**
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-21 15:27
 */
public class ForeignKeyException extends Exception{
    public ForeignKeyException(){super("Foreign Connect Failed");}
    public ForeignKeyException(int id) {
        super("Bill connect with "+ id);
    }

}
