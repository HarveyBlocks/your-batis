package org.harvey.batis.demo.exc;


import org.harvey.batis.demo.entity.Good;

/**
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-21 15:27
 */
public class IdNotFoundException extends Exception {
    public IdNotFoundException(int id) {
        super("Id " +
                (id != Good.DEFAULT_ID ?
                        id + " not found"
                        :
                        "haven't set"
                )
        );
    }

    public IdNotFoundException() {
        super();
    }
}
