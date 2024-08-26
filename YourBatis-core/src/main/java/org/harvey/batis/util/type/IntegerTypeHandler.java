package org.harvey.batis.util.type;

import org.harvey.batis.util.enums.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-20 22:53
 * @see ResultSet#getInt(int)
 * @see PreparedStatement#setInt(int, int)
 */
public class IntegerTypeHandler extends BaseTypeHandler<Integer> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int paramIndex, Integer parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(paramIndex, parameter);
    }


    @Override
    public Integer getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int result = rs.getInt(columnName);
        return result == 0 && rs.wasNull() ? null : result;
    }

    @Override
    public Integer getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int result = rs.getInt(columnIndex);
        return result == 0 && rs.wasNull() ? null : result;
    }

    @Override
    public Integer getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int result = cs.getInt(columnIndex);
        return result == 0 && cs.wasNull() ? null : result;
    }
}
