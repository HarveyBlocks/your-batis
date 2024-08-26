package org.harvey.batis.demo.entity;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-21 15:27
 */
public class GoodBill {
    public GoodBill(int billId, int goodId, int count) {
        this.billId = billId;
        this.goodId = goodId;
        this.count = count;
    }

    int billId;
    int goodId;
    int count;
}
