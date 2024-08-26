package org.harvey.batis.demo.mapper;



import org.harvey.batis.annotation.Param;
import org.harvey.batis.demo.entity.Bill;
import org.harvey.batis.demo.entity.Good;
import org.harvey.batis.demo.entity.GoodBill;

import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-31 23:21
 */
public interface BillMapper {
    //增
    int addBill(Bill bill);

    void addGoods(@Param("list") List<GoodBill> list);
    //删

    /*
     * 列
     * 适合仔细审查每一次删除是否成功
     * */
    int delById(int id);

    /*
     * 数组中可以只含一个元素来实现删除单个元素的目的
     * 而且不会出现id不存在的异常
     * 所以这个适合返回int值
     * 却不知道是哪里出了问题
     * */
    int delByIds(int[] ids);

    //改
    int updateByMessage(@Param("id") int id, @Param("customerId") int customerId, @Param("billDate") Date billDate);

    int update(Bill bill);


    //查
    List<Bill> selectAll();


    //条件
    List<Bill> selectById(@Param("id") int id);

    List<Bill> selectByIds(int[] id);


    List<Bill> selectByCustomerId(@Param("customerId") int customerId);

    List<Bill> selectByCustomerIds(int[] customerId);

    //排序
    /*
     * ASC(ascending,默认)升序
     * DESC(descending)降序
     * */
    List<Bill> orderByAsc(@Param("column") String column);

    List<Bill> orderByDesc(@Param("column") String column);


    //分页
    //参数 每页的page
    List<Bill> dividePage(@Param("start") int start, @Param("len") int len);




    /*
     * 外键
     * 返回一张表,返回商品
     * */
    List<Good> selectGoods(@Param("billId") int billId);
}
