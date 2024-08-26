package org.harvey.batis.demo.enums;


/**
 * 商品类
 * <p>
 * create table good(
 * id int(4) comment '主键,商品号' primary key auto_increment,
 * name varchar(50) comment '商品名,唯一' unique ,
 * price double comment '商品价格,即单价' not null,
 * stock int comment '库存' check (stock>=0)
 * )comment '商品表' ;
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-21 15:27
 */
public enum GoodColumn {
    ID,
    NAME,
    PRICE,
    STOCK
}
