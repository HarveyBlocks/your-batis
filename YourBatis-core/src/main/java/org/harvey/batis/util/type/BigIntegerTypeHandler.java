package org.harvey.batis.util.type;

import org.harvey.batis.util.enums.JdbcType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-23 22:49
 * @see ResultSet#getBigDecimal(int)
 * @see PreparedStatement#setBigDecimal(int, BigDecimal)
 */
public class BigIntegerTypeHandler extends BaseTypeHandler<BigInteger> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, BigInteger parameter, JdbcType jdbcType) throws SQLException {
        ps.setBigDecimal(i, new BigDecimal(parameter));
    }

    @Override
    public BigInteger getNullableResult(ResultSet rs, String columnName) throws SQLException {
        BigDecimal bigDecimal = rs.getBigDecimal(columnName);
        return bigDecimal == null ? null : bigDecimal.toBigInteger();
    }

    @Override
    public BigInteger getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        BigDecimal bigDecimal = rs.getBigDecimal(columnIndex);
        return bigDecimal == null ? null : bigDecimal.toBigInteger();
    }

    @Override
    public BigInteger getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        BigDecimal bigDecimal = cs.getBigDecimal(columnIndex);
        return bigDecimal == null ? null : bigDecimal.toBigInteger();
    }
}