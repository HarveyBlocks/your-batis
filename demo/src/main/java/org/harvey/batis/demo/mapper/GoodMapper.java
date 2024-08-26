package org.harvey.batis.demo.mapper;


import org.harvey.batis.annotation.Param;
import org.harvey.batis.demo.entity.Good;

import java.util.List;
import java.util.Map;

/**
 * 商品映射
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-31 23:21
 */
public interface GoodMapper {
    /**
     * 增
     */
    void addGood(Good good);

    void addGood2(Map<String, Object> good);


    /**
     * 以列删
     * 适合仔细审查每一次删除是否成功
     */
    int delById(int id);

    /*
     * 数组中可以只含一个元素来实现删除单个元素的目的
     * 而且不会出现id不存在的异常
     * 所以这个适合返回int值
     * 却不知道是哪里出了问题
     * */
    int delByIds(int[] ids);

    //改
    int update(Good good);

    //查
    List<Good> selectAll();

    //条件
    List<Good> selectById(
            @Param("id") int id);

    List<Good> selectByIds(int[] id);


    //对于名字我们要注意模糊查询
    List<Good> selectByName(
            @Param("name") String name);

    List<Good> selectByNames(String[] name);

    //闭区间
    List<Good> selectByPrices(
            @Param("low") double low,
            @Param("high") double high);

    List<Good> selectByPrice(
            @Param("price") double price);

    //闭区间
    List<Good> selectByStocks(
            @Param("low") double low,
            @Param("high") double high);

    List<Good> selectByStock(
            @Param("stock") int stock);

    List<Good> selectByCondition(Map<String, Object> map);

    //分组 没有意义不做了

    //排序

    /**
     * ASC(ascending,默认)升序
     * DESC(descending)降序
     *
     * @deprecated
     */
    List<Good> orderByAsc(
            @Param("column") String column);

    /**
     * ASC(ascending,默认)升序
     * DESC(descending)降序
     *
     * @deprecated
     */
    List<Good> orderByDesc(
            @Param("column") String column);

    List<Good> orderBy(
            @Param("rules") String[] rules
    );

    //分页
    //参数 每页的page
    List<Good> dividePage(
            @Param("start") int start,
            @Param("len") int len);


    List<Good> orderThenPage(
            @Param("column") String column,
            @Param("isDesc") boolean isDesc,
            @Param("start") int start,
            @Param("len") int len
    );


}


