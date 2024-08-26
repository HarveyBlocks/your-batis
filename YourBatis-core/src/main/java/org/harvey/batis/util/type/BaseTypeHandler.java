package org.harvey.batis.util.type;

import org.harvey.batis.exception.type.ResultMapException;
import org.harvey.batis.exception.type.TypeException;
import org.harvey.batis.util.enums.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @param <T> JavaType
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-13 21:14
 */
public abstract class BaseTypeHandler<T> extends TypeReference<T> implements TypeHandler<T> {
    /**
     * @param ps         向ps中paramIndex位置填入parameter<br>
     * @param paramIndex 参数需要填入的位置
     * @param parameter  参数具体的值, 可为null
     * @param jdbcType   数据库类型{@link JdbcType}, 可能为null
     * @throws TypeException 在parameter和jdbcType都为null的情况下<br>
     *                       在parameter为null,jdbcType不为null
     *                       且{@link PreparedStatement#setNull(int, int)}抛出异常的情况下
     * @see PreparedStatement
     * @see PreparedStatement#setNull(int, int)
     * @see #setNonNullParameter(PreparedStatement, int, Object, JdbcType)
     */
    @Override
    public void setParameter(PreparedStatement ps, int paramIndex, T parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null && jdbcType == null) {
            throw new TypeException("JDBC requires that the JdbcType must be specified for all nullable parameters.");
        }
        if (parameter == null) {
            try {
                ps.setNull(paramIndex, jdbcType.getTypeCode());
            } catch (SQLException e) {
                throw new TypeException("Error setting null for parameter #" + paramIndex + " with JdbcType " + jdbcType + " . "
                        + "Try setting a different JdbcType for this parameter or a different jdbcTypeForNull configuration property. "
                        + "Cause: " + e, e);
            }
        }
        try {
            this.setNonNullParameter(ps, paramIndex, parameter, jdbcType);
        } catch (Exception e) {
            throw new TypeException("Error setting non null for parameter #" + paramIndex + " with JdbcType " + jdbcType + " . "
                    + "Try setting a different JdbcType for this parameter or a different configuration property. "
                    + "Cause: " + e, e);
        }

    }

    /**
     * @throws ResultMapException 抛出异常后的包装
     * @see #getNullableResult(ResultSet, String)
     */
    @Override
    public T getResult(ResultSet rs, String columnName) throws SQLException {
        try {
            return getNullableResult(rs, columnName);
        } catch (Exception e) {
            throw new ResultMapException("Error attempting to get column '" + columnName + "' from result set.  Cause: " + e, e);
        }
    }

    /**
     * @throws ResultMapException 抛出异常后的包装
     * @see #getNullableResult(ResultSet, int)
     */
    @Override
    public T getResult(ResultSet rs, int columnIndex) throws SQLException {
        try {
            return getNullableResult(rs, columnIndex);
        } catch (Exception e) {
            throw new ResultMapException("Error attempting to get column #" + columnIndex + " from result set.  Cause: " + e, e);
        }
    }

    /**
     * @throws ResultMapException 抛出异常后的包装
     * @see #getNullableResult(CallableStatement, int)
     */
    @Override
    public T getResult(CallableStatement cs, int columnIndex) throws SQLException {
        try {
            return getNullableResult(cs, columnIndex);
        } catch (Exception e) {
            throw new ResultMapException("Error attempting to get column #" + columnIndex + " from callable statement.  Cause: " + e, e);
        }
    }


    /**
     * @param ps         向ps中paramIndex位置填入parameter<br>
     * @param paramIndex 参数需要填入的位置
     * @param parameter  参数具体的值, 不为null
     * @param jdbcType   数据库类型{@link JdbcType}, 可能为null
     * @see PreparedStatement
     * @see PreparedStatement#setInt(int, int)
     * @see PreparedStatement#setString(int, String)
     */
    public abstract void setNonNullParameter(PreparedStatement ps, int paramIndex, T parameter, JdbcType jdbcType) throws SQLException;

    /**
     * @return 从rs中获取columnName对应的值, <br>
     * 如果值不存在({@link ResultSet#wasNull()})就返回null
     * @see ResultSet
     * @see ResultSet#getInt(String)
     * @see ResultSet#getString(String)
     */
    public abstract T getNullableResult(ResultSet rs, String columnName) throws SQLException;

    /**
     * @return 从rs中获取columnIndex对应的值, <br>
     * 如果值不存在({@link ResultSet#wasNull()})就返回null
     * @see ResultSet
     * @see ResultSet#getInt(int)
     * @see ResultSet#getString(int)
     */
    public abstract T getNullableResult(ResultSet rs, int columnIndex) throws SQLException;


    /**
     * @return 从cs中获取columnIndex对应的值, <br>
     * 如果值不存在({@link CallableStatement#wasNull()})就返回null
     * @see CallableStatement
     * @see CallableStatement#getInt(int)
     * @see CallableStatement#getString(int)
     */
    public abstract T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException;

}
