package org.harvey.batis.demo.enums;


/**
 * create table bill(
 * id int(8) comment '主键,账单号' primary key auto_increment,
 * good_id int comment '商品id' not null,
 * constraint fk_bill_good_id foreign key(good_id) references good(id) ,
 * customer_id int(4) comment '用户id' not null ,
 * price   double comment '订单价格,实际交易金额,包括单价*数量,折扣优惠等一系列因素',
 * bill_date time comment '订单成交时间,年月日时分秒'
 * ) comment '账单表';
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-21 15:27
 */
public enum BillColumn {
    ID,
    COUNT,
    CUSTOM_ID,
    PRICE,
    BILL_DATE
}
