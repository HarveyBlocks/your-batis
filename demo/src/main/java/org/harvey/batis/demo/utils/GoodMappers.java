package org.harvey.batis.demo.utils;


import org.harvey.batis.demo.entity.Good;
import org.harvey.batis.demo.enums.GoodColumn;
import org.harvey.batis.demo.exc.*;
import org.harvey.batis.demo.mapper.GoodMapper;
import org.harvey.batis.session.SqlSession;
import org.harvey.batis.session.SqlSessionFactory;

import java.io.IOException;
import java.util.*;

/**
 * 这是一个类似与工具类的非工具类
 * 考虑到在写Mapper的时候,前面要加好长代码,就把这个代码抽出来,写到一个地方去了
 * 但是同时每次调用方法都会创建一个SqlSession的流,本来的话只要最前面写一个,不用创建这么多的,效率降低了?
 * 想使用这个类姑且是想用单例的,但也不知道用的对不对,好不好,有没有必要
 * 不能说是熟练账掌握了单例
 * 因此是个不伦不类的类
 * 这个类里主要还对Mapper里的方法进行了异常处理
 * 为了做异常处理,果然这个类还是有必要的
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-26 11:43
 */
public class GoodMappers {

    // 这是一个为了实现单例而创建的对象
    private static final GoodMappers GOOD_MAPPERS;

    // sqlSession的工厂方法,方便本类中的方法少写一点重复的过程
    private static final SqlSessionFactory SQL_SESSION_FACTORY = SqlSessionFactoryUtils.getFactory();

    // 静态内部类,用来实例化goodMappers,因为有个异常要抓取
    static {
        try {
            GOOD_MAPPERS = new GoodMappers();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private GoodMappers() throws IOException {
    }

    /**
     * 工厂方法,获取goodMappers对象
     *
     * @return goodMappers对象
     */
    public static GoodMappers goodMappersFactory() {
        return GOOD_MAPPERS;
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

    /**
     * 添加记录
     *
     * @param name  Good类里的商品名
     * @param price Good类里的商品价格
     * @param stock Good类里的库存
     * @throws StockMinusException 库存为负异常
     * @throws NameExistException  名字已经存在异常,因为设置了name unique
     */
    public void add(String name, double price, int stock)
            throws StockMinusException, NameExistException {
        if (name == null) {
            //可是我一想,name==null,也可以加给MySQL吧?逻辑上没问题吧?
            return;
        }
        if (stock < 0) {
            // 库存为负,抛出异常
            throw new StockMinusException(name, stock);
        }
        price = formatPrice(price);
        try (SqlSession sqlSession = SQL_SESSION_FACTORY.openSession()) {

            GoodMapper goodMapper = sqlSession.getMapper(GoodMapper.class);
            try {
                goodMapper.addGood(new Good(name, price, stock));
                sqlSession.commit();
            } catch (Exception e) {
                sqlSession.rollback();
                throw new NameExistException(name);
            }
        }
    }


    /**
     * 添加记录
     *
     * @param id Good类里的商品编号,主键,所以用来删除
     * @return 返回true, 成功删除一条记录;返回false,没能删除;不会有其他情况,因为id是主键
     * @throws IdNotFoundException 未找到要删除的记录的主键,即未删除
     */
    public boolean delById(int id)
            throws IdNotFoundException {
        try (SqlSession sqlSession = SQL_SESSION_FACTORY.openSession()) {
            GoodMapper goodMapper = sqlSession.getMapper(GoodMapper.class);
            try {
                if (goodMapper.delById(id) == 0) {
                    // 删除记录的条数是0,这个时候就是没找到主键了
                    throw new IdNotFoundException(id);
                }
                sqlSession.commit();
            } catch (IdNotFoundException e) {
                // 如果有其他的异常就不会被捕获,我还没有遇见过这种情况
                // 回滚是节操一样的东西,还是加上了
                sqlSession.rollback();
                throw e;
            }
        }
        return true;
    }

    /**
     * 根据id的数组来一次性删除多条记录
     * 如果遇到一个id没找到,若中断,回滚,返回0,不删除
     *
     * @param ids id的数组
     * @return 返回成功删除的元素个数, 好没用啊
     * @throws IdNotFoundException 未找到要删除的记录的主键,即未删除
     * @throws ForeignKeyException 删除了外键关联的元素时抛出异常
     */
    public int delById(int[] ids)
            throws IdNotFoundException, ForeignKeyException {
        if (ids == null) {
            return 0;
        }
        int num = 0; // num用于取得最后的成功删除的记录总数
        try (SqlSession sqlSession = SQL_SESSION_FACTORY.openSession()) {
            GoodMapper goodMapper = sqlSession.getMapper(GoodMapper.class);
            for (int id : ids) {
                try {
                    if (goodMapper.delById(id) == 0) {
                        throw new IdNotFoundException(id);
                    }
                    sqlSession.commit();
                } catch (IdNotFoundException idE) {
                    sqlSession.rollback();
                    throw idE;
                } catch (Exception e) {
                    sqlSession.rollback();
                    throw new ForeignKeyException(id);
                }

                num += 1;
            }
        }
        return num;
    }


    /**
     * 根据id的数组来一次性删除多条记录
     * 如果遇到一个id没找到,直接pass
     * 遇到外键关联的,直接中断
     *
     * @param ids id的数组
     * @return 返回成功删除的元素个数
     * @throws ForeignKeyException 删除了外键关联的元素时抛出异常
     */
    public int delByIds(int[] ids) throws ForeignKeyException {
        if (ids == null) {
            return 0;
        }
        int num;// num默认为0,idea说它冗余的赋值,可我觉得去掉会导致可读性变差
        try (SqlSession sqlSession = SQL_SESSION_FACTORY.openSession()) {
            GoodMapper goodMapper = sqlSession.getMapper(GoodMapper.class);
            try {
                num = goodMapper.delByIds(ids);
                sqlSession.commit();
            } catch (Exception e) {
                sqlSession.rollback();
                throw new ForeignKeyException();
            }
        }
        return num;
    }

    /**
     * 更新
     * 约束的异常:不用,因为id不会改
     *
     * @param id    Good类里的主键,商品编号
     * @param name  Good类里的商品名
     * @param price Good类里的商品价格
     * @param stock Good类里的库存
     * @return 更新是否会成功
     * @throws StockMinusException    库存为负异常
     * @throws IdNotFoundException    未找到要更改的记录的主键,即未删除
     * @throws NameExistException     名字已经存在异常,因为设置了name unique
     * @throws StringTooLongException 设置的参数name过长
     */
    public boolean updateByMessage(int id, String name, double price, int stock)
            throws StockMinusException, IdNotFoundException, NameExistException, StringTooLongException {
        return update(id, name, price, stock);
    }

    /**
     * 更新
     * 约束的异常:不用,因为id不会改
     *
     * @param id    Good类里的主键,商品编号
     * @param name  Good类里的商品名
     * @param price Good类里的商品价格
     * @param stock Good类里的库存
     * @return 更新是否会成功
     * @throws StockMinusException    库存为负异常
     * @throws IdNotFoundException    未找到要更改的记录的主键,即未删除
     * @throws NameExistException     名字已经存在异常,因为设置了name unique
     * @throws StringTooLongException 设置的参数name过长
     */
    public boolean update(int id, String name, double price, int stock)
            throws StockMinusException, IdNotFoundException, NameExistException, StringTooLongException {
        Good good = new Good(name, price, stock);
        good.setId(id);
        return update(good);
    }

    /**
     * 更新,Good类内部会检查参数是否合法
     *
     * @param good Good对象
     * @return 更新是否会成功
     * @throws IdNotFoundException 未找到要更改的记录的主键,即未删除
     * @throws NameExistException  名字已经存在异常,因为设置了name unique
     */
    public boolean update(Good good)
            throws IdNotFoundException, NameExistException {
        String name = good.getName();
        int id = good.getId();
        if (name == null) {
            return false;
        }
        try (SqlSession sqlSession = SQL_SESSION_FACTORY.openSession()) {
            GoodMapper goodMapper = sqlSession.getMapper(GoodMapper.class);
            try {
                if (goodMapper.update(good) == 0) {
                    throw new IdNotFoundException(id);
                }
                sqlSession.commit();
            } catch (IdNotFoundException idE) {
                sqlSession.rollback();
                throw idE;
            } catch (Exception e) {
                sqlSession.rollback();
                throw new NameExistException(name);
            }
        }
        return true;
    }

    /**
     * 查
     *
     * @return 查询所有的记录
     */
    public List<Good> selectAll() {
        try (SqlSession sqlSession = SQL_SESSION_FACTORY.openSession()) {
            GoodMapper goodMapper = sqlSession.getMapper(GoodMapper.class);
            return goodMapper.selectAll();
        }
    }

    /**
     * 依据商品id模糊查询
     *
     * @param id 依据商品id查询
     * @return 查询到的id符合的所有的记录
     */
    public List<Good> selectById(int id) {
        try (SqlSession sqlSession = SQL_SESSION_FACTORY.openSession()) {
            GoodMapper goodMapper = sqlSession.getMapper(GoodMapper.class);
            return goodMapper.selectById(id);
        }
    }


    /**
     * 依据商品id模糊查询
     *
     * @param ids 依据商品ids查询,或关系
     * @return 查询到的id符合的所有的记录
     */
    public List<Good> selectByIds(int[] ids) {
        if (ids == null) {
            return null;
        }
        try (SqlSession sqlSession = SQL_SESSION_FACTORY.openSession()) {
            GoodMapper goodMapper = sqlSession.getMapper(GoodMapper.class);
            return goodMapper.selectByIds(ids);
        }
    }

    /**
     * 依据商品name模糊查询
     *
     * @param name 依据商品name查询,或关系
     * @return 查询到的name符合的所有的记录
     */
    public List<Good> selectByName(String name) {
        if (name == null) {
            return null;
        }
        try (SqlSession sqlSession = SQL_SESSION_FACTORY.openSession()) {
            GoodMapper goodMapper = sqlSession.getMapper(GoodMapper.class);
            return goodMapper.selectByName(name);
        }
    }

    /**
     * 依据商品names模糊查询
     *
     * @param names 依据商品name查询,或关系,查询多个商品名
     * @return 查询到的name符合的所有的记录
     */
    public List<Good> selectByNames(String[] names) {
        if (names == null) {
            return null;
        }
        try (SqlSession sqlSession = SQL_SESSION_FACTORY.openSession()) {
            GoodMapper goodMapper = sqlSession.getMapper(GoodMapper.class);
            return goodMapper.selectByNames(names);
        }
    }

    /**
     * 依据商品价格上下限查询,闭区间
     *
     * @param low  商品价格下限
     * @param high 商品价格上限
     * @return 查询到的价格符合的所有的记录
     * @throws LowLargerThanHighException 下限高于上限
     */
    public List<Good> selectByPrices(double low, double high)
            throws LowLargerThanHighException {
        // 这里的价格也格式化下
        low = formatPrice(low);
        high = formatPrice(high);
        if (low > high) {
            throw new LowLargerThanHighException(low, high);
        }
        // 我不知道这样做合理与否,但姑且还是这么做了
        try (SqlSession sqlSession = SQL_SESSION_FACTORY.openSession()) {

            GoodMapper goodMapper = sqlSession.getMapper(GoodMapper.class);
            return goodMapper.selectByPrices(low, high);
        }

    }


    /**
     * 依据商品价格查询,在想要不要搞个在商品价格+-某个值之内的....方法
     *
     * @param price 商品价格
     * @return 查询到的价格符合的所有的记录
     */
    public List<Good> selectByPrice(double price) {
        price = formatPrice(price);
        //我不知道这样做合理与否,但姑且还是这么做了
        try (SqlSession sqlSession = SQL_SESSION_FACTORY.openSession()) {
            GoodMapper goodMapper = sqlSession.getMapper(GoodMapper.class);
            return goodMapper.selectByPrice(price);
        }
    }

    /**
     * 以库存数来获取
     *
     * @param stock 这个stock段位的库存
     * @return 所有符合库存数的记录
     * @throws StockMinusException 检查参数stock
     */
    public List<Good> selectByStock(int stock)
            throws StockMinusException {
        if (stock < 0) {
            throw new StockMinusException(stock);
        }
        // 我不知道这样做合理与否,但姑且还是这么做了
        try (SqlSession sqlSession = SQL_SESSION_FACTORY.openSession()) {
            GoodMapper goodMapper = sqlSession.getMapper(GoodMapper.class);
            return goodMapper.selectByPrice(stock);
        }
    }

    /**
     * 依据商品库存上下限查询,闭区间
     *
     * @param low  商品库存下限
     * @param high 商品库存上限
     * @return 查询到的价格符合的所有的记录
     * @throws StockMinusException        传入范围存在负数
     * @throws LowLargerThanHighException stock下限高于上限
     */
    public List<Good> selectByStocks(int low, int high)
            throws StockMinusException, LowLargerThanHighException {
        if (low > high) {
            throw new LowLargerThanHighException(low, high);
        }
        if (low < 0) {
            throw new StockMinusException(low);
        }
        // 我不知道这样做合理与否,但姑且还是这么做了
        try (SqlSession sqlSession = SQL_SESSION_FACTORY.openSession()) {
            GoodMapper goodMapper = sqlSession.getMapper(GoodMapper.class);
            return goodMapper.selectByStocks(low, high);
        }
    }


    /**
     * 本方法可以多条件查询,或关系,不支持上下限,不会检查数据
     * 没找到字段就selectAll
     *
     * @param map 一个Good属性-值对的集合
     * @return 返回查到的记录
     */
    public List<Good> selectByCondition(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        // 因为要遍历Map,对元素做判断,实现有点麻烦,还没有意义,所以果断放弃做异常
        try (SqlSession sqlSession = SQL_SESSION_FACTORY.openSession()) {
            GoodMapper goodMapper = sqlSession.getMapper(GoodMapper.class);
            return goodMapper.selectByCondition(map);
        }
    }


    /**
     * 排序
     * 多字段排序就算了,感觉用处不大?我再想想
     *
     * @param column 字段名GoodColumn是一个枚举类型
     * @param isAsc  是否降序
     * @return 返回记录的集合
     */
    public List<Good> orderBy(GoodColumn column, boolean isAsc) {
        if (column == null) {
            return null;
        }
        GoodColumn[] columns = {column};
        boolean[] asc = {isAsc};

        return orderBy(columns, asc);
    }

    /**
     * 排序
     * 所谓的"默认升序"
     *
     * @param column 字段名GoodColumn是一个枚举类型
     * @return 返回记录的集合
     */
    public List<Good> orderBy(GoodColumn column) {
        return orderBy(column, true);
    }

    /**
     * 对多字段排序的一点想法
     * 1. 很少用到
     * 2. 参数传入List,先后顺序
     * 3. 再建一个类,两个属性:字段名,升降
     * 4. 第三点直接把我劝退,我放弃
     * 5. <foreach></foreach>
     * Maybe可以想python一样两个表作为参数,一一对应
     * 但是异常很难做,字段对不对啊...等等,我好像可以了!
     */
    public List<Good> orderBy(GoodColumn[] columns, boolean[] isAsc) {
        if (columns == null) {
            return null;
        }
        try (SqlSession sqlSession = SQL_SESSION_FACTORY.openSession()) {

            //这里就不抛出异常了
            //取小的那个
            int len = Math.min(columns.length, isAsc.length);
            String[] rules = new String[len];
            for (int i = 0; i < len; i++) {
                rules[i] = columns[i].toString() + " " + (isAsc[i] ? "asc" : "desc");
            }
            return sqlSession.getMapper(GoodMapper.class).orderBy(rules);
        }
    }

    /*
     * 分页查询有什么好的,就不能对集合进行操作吗?
     * head()和tail()更本没有意义好吧
     * 直接selectAll然后对list操作要好多了好吧
     */


    /**
     * 取得前几条记录,因为id是自增的主键,所以算作是默认以id升序排序
     *
     * @param len 取得的长度
     * @return 返回后几个Good然后组成集合, id升序
     */
    public List<Good> head(int len) {
        try (SqlSession sqlSession = SQL_SESSION_FACTORY.openSession()) {
            return sqlSession.getMapper(GoodMapper.class).dividePage(0, len);
        }
    }

    /**
     * 最后的几个,得到之后倒置
     *
     * @param len 取得的长度
     * @return 返回后几个Good然后组成集合, id降序
     */
    public List<Good> tail(int len) {

        try (SqlSession sqlSession = SQL_SESSION_FACTORY.openSession()) {
            GoodMapper goodMapper = sqlSession.getMapper(GoodMapper.class);
            List<Good> all = goodMapper.selectAll();
            int length = all.size();
            List<Good> list = goodMapper.dividePage(length - len, len);
            Collections.reverse(list);
            return list;
        }
    }


    /**
     * 分页
     *
     * @param len 每页长度
     * @return 返回List
     */
    public List<List<Good>> dividePagesToList(int len) {
        try (SqlSession sqlSession = SQL_SESSION_FACTORY.openSession()) {
            GoodMapper goodMapper = sqlSession.getMapper(GoodMapper.class);
            List<Good> all = goodMapper.selectAll();
            int length = all.size();
            List<List<Good>> list = new ArrayList<List<Good>>();
            if (length == 0 || len > length) {
                list.add(all);
                return list;
            }
            for (int i = 0; i < length / len + (length % len == 0 ? 0 : 1); i++) {
                list.add(goodMapper.dividePage(i * len, len));
            }
            return list;
        }
    }

    /**
     * 分页
     *
     * @param len 每页长度
     * @return 返回Map<页码, 内容List>
     */
    public Map<Integer, List<Good>> dividePagesToMap(int len) {
        try (SqlSession sqlSession = SQL_SESSION_FACTORY.openSession()) {
            GoodMapper goodMapper = sqlSession.getMapper(GoodMapper.class);
            List<Good> all = goodMapper.selectAll();
            int length = all.size();
            Map<Integer, List<Good>> map = new HashMap<>();
            if (length == 0 || len > length) {
                map.put(1, all);
                return map;
            }
            for (int i = 0; i < length / len + (length % len == 0 ? 0 : 1); i++) {
                map.put(i + 1, goodMapper.dividePage(i * len, len));
            }
            return map;
        }
    }


    /**
     * 排序再分页查询
     *
     * @return 返回排序分页后的list
     */
    public List<List<Good>> dividePagesOrderBy(GoodColumn column, boolean isAsc, int len) {
        if (column == null) {
            return null;
        }
        try (SqlSession sqlSession = SQL_SESSION_FACTORY.openSession()) {
            GoodMapper goodMapper = sqlSession.getMapper(GoodMapper.class);
            List<Good> all = goodMapper.selectAll();
            int length = all.size();
            List<List<Good>> list = new ArrayList<>();
            if (length == 0 || len > length) {
                list.add(all);
                return list;
            }
            all = null;
            for (int i = 0; i < length / len + (length % len == 0 ? 0 : 1); i++) {
                list.add(goodMapper.orderThenPage(column.toString(), isAsc, i * len, len));
            }
            return list;
        }
    }

    /**
     * 默认升序
     * 排序再分页查询
     *
     * @return 返回排序分页后的list
     */
    public List<List<Good>> dividePagesOrderBy(GoodColumn column, int len) {
        return dividePagesOrderBy(column, false, len);
    }

    /**
     * 排序后再取得头
     *
     * @param column 参与排序的字段
     * @param len    取得的长度
     */
    public List<Good> headOrderBy(GoodColumn column, int len) {
        return dividePagesOrderBy(column, true, len).get(0);
    }

    /**
     * 最后的几个,得到之后倒置
     *
     * @param column 参与排序的字段
     * @param len    取得的长度
     * @return 返回后几个Good然后组成集合, id降序
     */
    public List<Good> tailOrderBy(GoodColumn column, int len) {
        return dividePagesOrderBy(column, false, len).get(0);
    }
}
