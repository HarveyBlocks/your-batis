package org.harvey.batis.util.type;

import org.harvey.batis.util.enums.JdbcType;

import java.sql.*;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-26 12:15
 * @see java.sql.Timestamp
 */
public class SqlTimestampTypeHandler extends BaseTypeHandler<Timestamp> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Timestamp parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setTimestamp(i, parameter);
    }

    @Override
    public Timestamp getNullableResult(ResultSet rs, String columnName)
            throws SQLException {
        return rs.getTimestamp(columnName);
    }

    @Override
    public Timestamp getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException {
        return rs.getTimestamp(columnIndex);
    }

    @Override
    public Timestamp getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        return cs.getTimestamp(columnIndex);
    }
}
