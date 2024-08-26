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
 * @see ResultSet#getLong(int)
 * @see PreparedStatement#setLong(int, long)
 */
public class LongTypeHandler extends BaseTypeHandler<Long> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int paramIndex, Long parameter, JdbcType jdbcType) throws SQLException {
        ps.setLong(paramIndex, parameter);
    }


    @Override
    public Long getNullableResult(ResultSet rs, String columnName) throws SQLException {
        long result = rs.getLong(columnName);
        return result == 0 && rs.wasNull() ? null : result;
    }

    @Override
    public Long getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        long result = rs.getLong(columnIndex);
        return result == 0 && rs.wasNull() ? null : result;
    }

    @Override
    public Long getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        long result = cs.getLong(columnIndex);
        return result == 0 && cs.wasNull() ? null : result;
    }
}
