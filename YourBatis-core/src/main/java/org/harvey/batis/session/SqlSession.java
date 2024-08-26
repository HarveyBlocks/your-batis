package org.harvey.batis.session;

import org.harvey.batis.config.Configuration;
import org.harvey.batis.executor.BatchResult;

import java.io.Closeable;
import java.util.List;

/**
 * TODO
 * 会话
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-02 12:32
 */
public interface SqlSession extends Closeable {
    /**
     * TODO
     * 关闭当前会话
     */
    @Override
    void close();

    /**
     * TODO
     * 获取一个Mapper
     *
     * @param <T>  Mapper的类型
     * @param type Mapper的接口类型
     * @return 绑定到此 SqlSession 的 Mapper
     */
    <T> T getMapper(Class<T> type);


    Configuration getConfiguration();

    /**
     * Flushes(刷新) batch statements(语句).
     *
     * @return 已更新记录的 BatchResult 列表
     */
    List<BatchResult> flushStatements();

    /**
     * 依据sql语句statement和参数从数据库获取一行记录。
     *
     * @param <T>       返回值类型
     * @param statement 唯一标识符, 与要使用的语句(?)匹配?TODO
     * @param parameter A parameter object to pass to the statement.
     * @return 获取到的结果
     */
    <T> T selectOne(String statement, Object parameter);

    /**
     * @see #selectList(String, Object, RowBounds)
     */
    <E> List<E> selectList(String statement, Object parameter);

    /**
     * 在指定的行边界内, 依据sql语句statement和参数从数据库获取一个list的记录。
     *
     * @param <E>       返回值类型
     * @param statement 唯一标识符, 与要使用的语句(?)匹配?TODO
     * @param parameter A parameter object to pass to the statement.
     * @param rowBounds 限制对象检索的边界?
     * @return 获取到的结果的List
     */
    <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds);


    /**
     * 执行Insert语句
     *
     * @param statement 执行语句的唯一标识
     * @return 添加到数据库中的记录个数
     */
    int insert(String statement);

    /**
     * 执行Insert语句
     *
     * @param statement 执行语句的唯一标识
     * @param parameter 需要被填充入语句的参数实体
     * @return 添加到数据库中的记录个数
     */
    int insert(String statement, Object parameter);

    /**
     * 执行Update语句
     *
     * @param statement 执行语句的唯一标识
     * @return 数据库中被影响到的记录个数
     */
    int update(String statement);

    /**
     * 执行Update语句
     *
     * @param statement 执行语句的唯一标识
     * @param parameter 需要被填充入语句的参数实体
     * @return 数据库中被影响到的记录个数
     */
    int update(String statement, Object parameter);

    /**
     * 执行delete语句
     *
     * @param statement 执行语句的唯一标识
     * @return 数据库中被影响到的记录个数
     */
    int delete(String statement);

    /**
     * 执行delete语句
     *
     * @param statement 执行语句的唯一标识
     * @param parameter 需要被填充入语句的参数实体
     * @return 数据库中被影响到的记录个数
     */
    int delete(String statement, Object parameter);
    void commit();


    void commit(boolean force);


    void rollback();

    void rollback(boolean force);
}
