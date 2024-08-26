package org.harvey.batis.executor;


import org.harvey.batis.cache.Cache;
import org.harvey.batis.cache.CacheKey;
import org.harvey.batis.cursor.Cursor;
import org.harvey.batis.executor.result.DefaultMapResultHandler;
import org.harvey.batis.executor.result.DefaultResultHandler;
import org.harvey.batis.executor.result.ResultHandler;
import org.harvey.batis.mapping.BoundSql;
import org.harvey.batis.mapping.MappedStatement;
import org.harvey.batis.reflection.MetaObject;
import org.harvey.batis.session.RowBounds;
import org.harvey.batis.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-02 13:51
 */
public interface Executor {
    ResultHandler NO_RESULT_HANDLER = null;


    /**
     * 执行写语句
     *
     * @return TODO
     */
    int update(MappedStatement ms, Object parameter) throws SQLException;


    /**
     * @param ms            映射语句, 映射关系是Mapper接口中的方法和XML文件中的SQL语句
     * @param parameter     参数, 用于向SQL语句中的?填入具体的值
     * @param rowBounds     上下限
     * @param resultHandler 结果从jdbcType到JavaType的映射, 见{@link ResultHandler}. <br>
     *                      一般是{@link DefaultResultHandler}和{@link DefaultMapResultHandler}
     * @param cacheKey      从缓存中获取缓存内容的键, 依据参数保证唯一性{@link CacheKey}
     * @param boundSql      TODO
     * @param <E>           返回的结果类型
     * @return 从数据库中查询, 然后返回查询到的结果
     * @throws SQLException 查询过程中出现的异常
     */
    <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds,
                      ResultHandler<?> resultHandler, CacheKey cacheKey, BoundSql boundSql) throws SQLException;


    /**
     * @see #query(MappedStatement, Object, RowBounds, ResultHandler, CacheKey, BoundSql)
     */
    <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler<?> resultHandler) throws SQLException;

    <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException;

    /**
     * TODO
     *
     * @return TODO
     */
    List<BatchResult> flushStatements() throws SQLException;

    void commit(boolean required) throws SQLException;

    void rollback(boolean required) throws SQLException;

    CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql);

    boolean isCached(MappedStatement ms, CacheKey key);

    /**
     * @see Cache#clear()
     */
    void clearLocalCache();

    void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType);

    Transaction getTransaction();

    void close(boolean forceRollback);

    boolean isClosed();

    void setExecutorWrapper(Executor executor);

}
