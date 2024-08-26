package org.harvey.batis.util.type;

import org.harvey.batis.util.enums.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-26 12:08
 */
public class DoubleTypeHandler extends BaseTypeHandler<Double> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Double parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setDouble(i, parameter);
    }

    @Override
    public Double getNullableResult(ResultSet rs, String columnName)
            throws SQLException {
        double result = rs.getDouble(columnName);
        return result == 0 && rs.wasNull() ? null : result;
    }

    @Override
    public Double getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException {
        double result = rs.getDouble(columnIndex);
        return result == 0 && rs.wasNull() ? null : result;
    }

    @Override
    public Double getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        double result = cs.getDouble(columnIndex);
        return result == 0 && cs.wasNull() ? null : result;
    }

}