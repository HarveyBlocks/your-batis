```mysql
CREATE TABLE `bill` (
                        `id` int NOT NULL AUTO_INCREMENT COMMENT '主键,账单号',
                        `customer_id` int NOT NULL COMMENT '用户id',
                        `bill_date` datetime DEFAULT NULL COMMENT '订单成交时间,年月日时分秒',
                        PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='账单表';
```
```mysql
create table good(
                     id int(5) comment '主键,商品号' primary key auto_increment,
                     name varchar(15) comment '商品名,唯一' unique ,
                     price float comment '商品价格,即单价' not null,
                     stock int comment '库存' check (stock>=0)
)comment '商品表' ;
```
```mysql
CREATE TABLE `good_bill` (
                             `id` int NOT NULL AUTO_INCREMENT,
                             `good_id` int DEFAULT NULL,
                             `bill_id` int DEFAULT NULL,
                             PRIMARY KEY (`id`),
                             KEY `fk_gb_bid_bill_id` (`bill_id`),
                             KEY `fk_gb_gid_good_id` (`good_id`),
                             CONSTRAINT `fk_gb_bid_bill_id` FOREIGN KEY (`bill_id`) REFERENCES `bill` (`id`),
                             CONSTRAINT `fk_gb_gid_good_id` FOREIGN KEY (`good_id`) REFERENCES `good` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='bill和good的联合表,bill和good多对多'

```