package org.harvey.batis.demo.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.List;


/**
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-21 15:27
 */
@Data
@NoArgsConstructor
public class Bill {
    private int id;

    private List<Good> goods;
    private int count;
    private int customerId;

    // 一次订单的总价
    private double price;

    private java.sql.Timestamp billDate;

    public Bill(int id, int customerId, java.sql.Timestamp billDate) {
        this.customerId = customerId;
        this.billDate = billDate;
        this.id = id;
    }

    public Bill(int customerId, java.sql.Timestamp billDate) {
        this.customerId = customerId;
        this.billDate = billDate;
    }



    private String formatDate(String billDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //格式化date
        return sdf.format(billDate);
    }


    public String goodsStrFormat() {
        StringBuilder str = new StringBuilder();
        if (goods!=null){
            for (Good good : goods) {
                str.append(good.toString()).append('\n');
            }
        }
        return String.valueOf(str);
    }

    public String strFormat() {
        String formatStr = this.getClass().getSimpleName() +
                "{" +
                "id=" + "%05d" +
                ", count=" + "%5d" +
                ", customerId=" + "%05d" +
                ", price=" + "%06.2f" +
                ", billDate=" + "%s" +
                '}';
        return String.format(formatStr, id, count, customerId, price, billDate);

    }

    public static String listToString(List<Bill> bills) {
        StringBuilder strList = new StringBuilder(
                "Bill List\n" +
                        "ID\t\t\t\tcount\t\t\tCustomer ID\t\tPrice\t\tBill Date\n");
        String formatStr =
                "%05d\t%5d\t\t%05d\t\t%06.2f\t%s\n";
        for (Bill bill : bills) {
            strList.append(String.format(
                    formatStr,
                    bill.id,
                    bill.count,
                    bill.customerId,
                    bill.price,
                    bill.billDate
            ));
        }
        return strList.toString();
    }

    @Override
    public String toString() {
        return "Bill{" +
                "id=" + id +
                ", count=" + count +
                ", customerId=" + customerId +
                ", price=" + price +
                ", billDate=" + billDate +
                ", goods={\n" + goodsStrFormat() +
                '}';
    }

}
