package org.harvey.batis.demo;


import org.harvey.batis.demo.entity.Good;
import org.harvey.batis.demo.enums.GoodColumn;
import org.harvey.batis.demo.exc.LowLargerThanHighException;
import org.harvey.batis.demo.exc.StockMinusException;
import org.harvey.batis.demo.utils.GoodMappers;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 有太多方法可以写了!<br>
 * 要不要写一次性增删改多个记录的方法?要不要重载一个以实体类为参数的方法?<br>
 * 等等等等,我都晕乎乎的了<br>
 * 所以最后,有些写了,有些没写'<br>
 * 从逻辑上讲外键约束就是默认的NO_ACTION就没问题<br>
 */
public class GoodTest {
    public static void main(String[] args) {
        GoodTest gt = new GoodTest();
        gt.testAD();
        gt.testDBI();
        gt.testUpdate();
        gt.testSelect();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger("ShopLog");
    private static final GoodMappers GOOD_MAPPERS = GoodMappers.goodMappersFactory();

    /*
     * 添加
     * */
    @Test
    public void testAD() {
        try {
            GOOD_MAPPERS.add("大波浪薯片味", 7.123412, 1000);
            LOGGER.info("添加成功");
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            LOGGER.error("添加失败");
        }
    }

    /*
     * 删除
     * */
    @Test
    public void testDBI() {
        try {
            int[] ids2 = {26, 32, 14};
            int i = GOOD_MAPPERS.delByIds(ids2);
            if (i == 0) {
                LOGGER.info("没有什么可以删除的");
            } else {
                LOGGER.info("删除" + i + "项,删除成功");
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            LOGGER.error("删除失败");
        }
    }


    /*
     * 更新与修改
     * */
    @Test
    public void testUpdate() {
        try {
            GOOD_MAPPERS.update(30, "大波浪薯片咖喱味", 7.12, 100);
            LOGGER.info("更新成功");
        } catch (Exception e) {
            LOGGER.error("更新失败");
            LOGGER.error(e.getMessage());
        }
    }

    @Test
    public void testSelect() {

        LOGGER.info("========All========");
        List<Good> goods = GOOD_MAPPERS.selectAll();
        goods.forEach(s -> LOGGER.info(s.toString()));
        LOGGER.info("=======dividePagesTo=============");
        List<List<Good>> lists = GOOD_MAPPERS.dividePagesToList(3);
        for (List<Good> list : lists) {
            list.forEach(s -> LOGGER.info(s.toString()));
            LOGGER.info("----------------------------");
        }
        LOGGER.info("========ById=========");

        GOOD_MAPPERS.selectById(12).forEach(s -> LOGGER.info(s.toString()));
        LOGGER.info("--------------------------");
        GOOD_MAPPERS.selectById(20).forEach(s -> LOGGER.info(s.toString()));
        LOGGER.info("--------------------------");
        GOOD_MAPPERS.selectById(2).forEach(s -> LOGGER.info(s.toString()));

        LOGGER.info("--------ByIds--------");


        int[] ids = {12, 20, 2};
        GOOD_MAPPERS.selectByIds(ids).forEach(s -> LOGGER.info(s.toString()));


        LOGGER.info("=========ByName======");

        String name = "味";
        GOOD_MAPPERS.selectByName(name).forEach(s -> LOGGER.info(s.toString()));

        LOGGER.info("------ByNames--------------");
        String[] names = {"薯", "番茄"};
        GOOD_MAPPERS.selectByNames(names).forEach(s -> LOGGER.info(s.toString()));


        LOGGER.info("==========ByPrice=======");

        GOOD_MAPPERS.selectByPrice(3).forEach(s -> LOGGER.info(s.toString()));
        GOOD_MAPPERS.selectByPrice(3.00004).forEach(s -> LOGGER.info(s.toString()));

        LOGGER.info("----------ByPrices---------");
        try {
            GOOD_MAPPERS.selectByPrices(2, 4).forEach(s -> LOGGER.info(s.toString()));
            GOOD_MAPPERS.selectByPrices(3, 3).forEach(s -> LOGGER.info(s.toString()));
        } catch (LowLargerThanHighException e) {
            LOGGER.error(e.getMessage());
        }
        try {
            GOOD_MAPPERS.selectByPrices(4, 2).forEach(s -> LOGGER.info(s.toString()));
        } catch (LowLargerThanHighException e) {
            LOGGER.error(e.getMessage());
        }

        LOGGER.info("==========ByStock=======");
        try {
            GOOD_MAPPERS.selectByStock(30).forEach(s -> LOGGER.info(s.toString()));
        } catch (StockMinusException e) {
            LOGGER.error(e.getMessage());
        }
        try {
            GOOD_MAPPERS.selectByStock(-3).forEach(s -> LOGGER.info(s.toString()));
        } catch (StockMinusException e) {
            LOGGER.error(e.getMessage());
        }
        LOGGER.info("----------ByStocks---------");
        try {
            GOOD_MAPPERS.selectByStocks(20, 400).forEach(s -> LOGGER.info(s.toString()));
            GOOD_MAPPERS.selectByStocks(30, 30).forEach(s -> LOGGER.info(s.toString()));
        } catch (LowLargerThanHighException | StockMinusException e) {
            LOGGER.error(e.getMessage());
        }
        try {
            GOOD_MAPPERS.selectByStocks(1000, 200).forEach(s -> LOGGER.info(s.toString()));
        } catch (StockMinusException | LowLargerThanHighException e) {
            LOGGER.error(e.getMessage());
        }
        try {
            GOOD_MAPPERS.selectByStocks(-4000, 2000).forEach(s -> LOGGER.info(s.toString()));
        } catch (StockMinusException | LowLargerThanHighException e) {
            LOGGER.error(e.getMessage());
        }


        LOGGER.info("===========ByCondition=========");
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("你好", "薯片");
            GOOD_MAPPERS.selectByCondition(map).forEach(s -> LOGGER.info(s.toString()));
        } catch (Exception e) {
            for (StackTraceElement ste : e.getStackTrace()) {
                LOGGER.error(ste.toString());
            }
        }
        //我突然发现,如果名字不能重复,那按实体类查询不是毫无意义?!


        LOGGER.info("==========sort==========");
        GOOD_MAPPERS.orderBy(GoodColumn.PRICE, true).forEach(g -> LOGGER.info(g.toString()));

        LOGGER.info("-------------------------");
        GOOD_MAPPERS.orderBy(GoodColumn.STOCK).forEach(g -> LOGGER.info(g.toString()));
        LOGGER.info("-------------------------");
        GOOD_MAPPERS.orderBy(GoodColumn.PRICE, true).forEach(g -> LOGGER.info(g.toString()));

        LOGGER.info("============head===========");
        GOOD_MAPPERS.head(2).forEach(g -> LOGGER.info(g.toString()));
        LOGGER.info("-----------tail-----------");
        GOOD_MAPPERS.tail(2).forEach(g -> LOGGER.info(g.toString()));

    }

    @Test
    public void testOrders() {
        GoodColumn[] columns = new GoodColumn[]{GoodColumn.ID, GoodColumn.PRICE};
        boolean[] isAsc = new boolean[]{true, false};
        List<Good> goods = GOOD_MAPPERS.orderBy(columns, isAsc);
        goods.forEach(System.out::println);
    }

}
