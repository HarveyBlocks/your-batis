package org.harvey.batis.demo.entity;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.harvey.batis.demo.exc.StockMinusException;
import org.harvey.batis.demo.exc.StringTooLongException;

import static org.harvey.batis.demo.utils.Common.fullName;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-21 15:27
 */
@Data
@NoArgsConstructor
public class Good {
    public static final int NAME_LEN_LIMIT = 15;
    public static final int DEFAULT_ID = -1;
    private int id;
    private String name;
    private double price;
    private int stock;

    public Good(String name, double price, int stock) throws StringTooLongException, StockMinusException {
        if (name.length() > NAME_LEN_LIMIT) {
            throw new StringTooLongException(name, NAME_LEN_LIMIT);
        }
        if (stock < 0) {
            throw new StockMinusException(name, stock);
        }
        this.name = name;
        this.price = formatPrice(price);
        this.stock = stock;
        this.id = DEFAULT_ID;
    }

    private static double formatPrice(double price) {
        String fPrice = String.format("%.2f", price);
        return Double.parseDouble(fPrice);
    }

    @Override
    public String toString() {
        String formatStr = this.getClass().getSimpleName() +
                "{" +
                "id=" + "%05d" +
                ", name=" + "%s" +
                ", price=" + "%06.2f" +
                ", stock=" + "%04d" +
                "}\n";
        return String.format(formatStr, id, fullName(name), price, stock);
    }


}
