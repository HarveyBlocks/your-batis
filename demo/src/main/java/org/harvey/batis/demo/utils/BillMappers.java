package org.harvey.batis.demo.utils;


import org.harvey.batis.demo.entity.Bill;
import org.harvey.batis.demo.entity.Good;
import org.harvey.batis.demo.entity.GoodBill;
import org.harvey.batis.demo.exc.ForeignKeyException;
import org.harvey.batis.demo.mapper.BillMapper;
import org.harvey.batis.session.SqlSession;
import org.harvey.batis.session.SqlSessionFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-26 11:43
 */
public class BillMappers {

    // 这是一个为了实现单例而创建的对象
    private static final BillMappers BILL_MAPPERS;

    // sqlSession的工厂方法,方便本类中的方法少写一点重复的过程
    private static SqlSessionFactory sqlSessionFactory = SqlSessionFactoryUtils.getFactory();

    // 静态内部类,用来实例化goodMappers,因为有个异常要抓取
    static {
        try {
            BILL_MAPPERS = new BillMappers();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @throws IOException getResourceAsStream抛出的异常
     */
    private BillMappers() throws IOException {
    }

    /**
     * 工厂方法,获取goodMappers对象
     *
     * @return goodMappers对象
     */
    public static BillMappers billMappersFactory() {
        return BILL_MAPPERS;
    }

    /**
     * 把价钱保留到两位小数
     *
     * @param price 待格式化的价格
     * @return 返回格式化之后的价格
     */
    private double formatPrice(double price) {
        String fPrice = String.format("%.2f", price);
        return Double.parseDouble(fPrice);
    }

    public boolean del(int id) throws ForeignKeyException {
        boolean isDel = false;
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                // 获取BillMapper接口的代理对象
                BillMapper billMapper = sqlSession.getMapper(BillMapper.class);
                isDel =billMapper.delById(id)!=0 ;
                sqlSession.commit();
            } catch (Exception e) {
                throw new ForeignKeyException(id);
            }
        }
        return isDel;
    }

    public void add(Bill bill, int[] ids, int[] counts) throws ForeignKeyException {
        if (bill == null) {
            return;
        }
        if (ids.length!=counts.length){
            throw new IllegalArgumentException("length of ids isn,t equals to counts'");
        }
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try {
                // 获取BillMapper接口的代理对象
                String[] map = new String[ids.length];

                BillMapper mapper = sqlSession.getMapper(BillMapper.class);
                int billId = mapper.addBill(bill);
                List<GoodBill> list = new ArrayList<>();
                for (int i = 0; i < counts.length; i++) {
                    list.add(new GoodBill(billId,ids[i],counts[i]));
                }
                mapper.addGoods(list);
                sqlSession.commit();
            } catch (Exception e) {
                throw new ForeignKeyException();
            }
        }
    }

    public int update(Bill bill) throws Exception {
        if (bill == null) {
            return 0;
        }
        int num;
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            // 获取BillMapper接口的代理对象
            num = sqlSession.getMapper(BillMapper.class).update(bill);
            sqlSession.commit();
        } catch (Exception e){
            e.printStackTrace();
            throw new ForeignKeyException();
        }
        return num;
    }

    /**
     * 查
     *
     * @return 查询所有的记录
     */
    public List<Bill> selectAll() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            // 获取BillMapper接口的代理对象
            BillMapper billMapper = sqlSession.getMapper(BillMapper.class);
            List<Bill> bills = billMapper.selectAll();
            bills.forEach(this::setGoods);
            return bills;
        }
    }

    public void setGoods(Bill bill) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            BillMapper billMapper = sqlSession.getMapper(BillMapper.class);
            List<Good> goods = billMapper.selectGoods(bill.getId());
            bill.setGoods(goods);
        }
    }
}


