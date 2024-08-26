package org.harvey.batis.util.type;

import org.harvey.batis.config.Configuration;
import org.harvey.batis.util.enums.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * TypeHandler, 一定是一边对应一个JDBC的类型, 一边对应一个Java的类型的<br>
 * 所以复合类型(Bean)是不能用TypeHandler的<br>
 * 但是枚举可以转为int or string, 故可以用TypeHandler<br>
 * 如果使用{@link JdbcType#JAVA_OBJECT}的话, 实际上进行检验的
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-10 16:30
 */
public interface TypeHandler<T> {

    /**
     * @param ps         向ps中paramIndex位置填入parameter<br>
     * @param paramIndex 参数需要填入的位置
     * @param parameter  参数具体的值
     * @param jdbcType   数据库类型{@link JdbcType}, 可能为null
     */
    void setParameter(PreparedStatement ps, int paramIndex, T parameter, JdbcType jdbcType) throws SQLException;

    /**
     * @param columnName {@link Configuration#isUseColumnLabel()}为false
     */
    T getResult(ResultSet rs, String columnName) throws SQLException;

    T getResult(ResultSet rs, int columnIndex) throws SQLException;

    T getResult(CallableStatement cs, int columnIndex) throws SQLException;

}