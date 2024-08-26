package org.harvey.batis.demo;


import org.harvey.batis.demo.entity.Bill;
import org.harvey.batis.demo.exc.ForeignKeyException;
import org.harvey.batis.demo.utils.BillMappers;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Calendar;


public class BillTest {
    private static final Logger LOGGER = LoggerFactory.getLogger("ShopLog");
    private static final BillMappers BILL_MAPPERS = BillMappers.billMappersFactory();
    @Test
    public void testAdd()  {
        LOGGER.info("--------------testAdd----------------");
        try {
            BILL_MAPPERS.add(new Bill(29,7, new Timestamp(Calendar.getInstance().getTimeInMillis())),new int[]{1,2,3},new int[]{1,1,2});
            LOGGER.info("添加成功");
        } catch (ForeignKeyException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Test
    public void delTest() {
        LOGGER.info("--------------testDel----------------");
        try {
            boolean del = BILL_MAPPERS.del(23);
            System.out.println(del);
            // LOGGER.info("删除成功");
        } catch (ForeignKeyException e) {
            LOGGER.error(e.getMessage());
        }
    }
    @Test
    public void updateTest()  {
        LOGGER.info("--------------testUpdate----------------");
        Bill bill = new Bill(5,new Timestamp(Calendar.getInstance().getTimeInMillis()));

        bill.setId(35);

        try {
            int update = BILL_MAPPERS.update(bill);
            LOGGER.info("更新"+update+"个");
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

    }
    @Test
    public void selectTest(){
        LOGGER.info("--------------testSelect----------------");
        Bill bill = new Bill();
        bill.setId(1);
        BILL_MAPPERS.setGoods(bill);
        LOGGER.info(bill.toString());
        LOGGER.info('\n'+bill.goodsStrFormat());
        LOGGER.info("---------testSelect-------------");
        //对toString还要做一个更改
        BILL_MAPPERS.selectAll().forEach(s->LOGGER.info('\n'+s.toString()));
    }

}
