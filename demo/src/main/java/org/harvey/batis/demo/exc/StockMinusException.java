package org.harvey.batis.demo.exc;

/**
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-21 15:27
 */
public class StockMinusException extends Exception {
    public StockMinusException() {
        super();
    }
    public StockMinusException(String name,int stock){
        super(name+"'s stock "+stock+" can not be minus");
    }
    public StockMinusException(int stock){
        super("stock "+stock+" can not be minus");
    }
}
