package org.harvey.batis.scripting;

import org.harvey.batis.config.Configuration;
import org.harvey.batis.executor.param.DefaultParameterHandler;
import org.harvey.batis.executor.param.ParameterHandler;
import org.harvey.batis.mapping.BoundSql;
import org.harvey.batis.mapping.MappedStatement;
import org.harvey.batis.mapping.sqlsource.SqlSource;
import org.harvey.batis.parsing.XNode;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-11 17:44
 */
public interface LanguageDriver {
    SqlSource createSqlSource(Configuration configuration, XNode context, Class<?> parameterType);

    /**
     * 创建 {@link ParameterHandler} 将实际参数传递给 JDBC 语句
     *
     * @param mappedStatement 需要被执行的Mapper语句
     * @param parameterObject 被输入的参数, 可为null
     * @param boundSql        执行动态语言后生成的SQL
     * @return {@link ParameterHandler}
     * @see DefaultParameterHandler
     */
    ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql);
}
