package org.harvey.batis.util.type;

import org.harvey.batis.config.Configuration;
import org.harvey.batis.exception.UnfinishedFunctionException;
import org.harvey.batis.util.enums.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-13 21:13
 */
public class UnknownTypeHandler extends BaseTypeHandler<Object> {
    private static final ObjectTypeHandler OBJECT_TYPE_HANDLER = new ObjectTypeHandler();
    private final Configuration config;

    /**
     * The constructor that pass a MyBatis configuration.
     *
     * @param configuration a MyBatis configuration
     * @since 3.5.4
     */
    public UnknownTypeHandler(Configuration configuration) {
        this.config = configuration;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int paramIndex, Object parameter, JdbcType jdbcType)
            throws SQLException {
        TypeHandler handler = this.resolveTypeHandler(parameter, jdbcType);
        handler.setParameter(ps, paramIndex, parameter, jdbcType);
    }
    private TypeHandler<?> resolveTypeHandler(Object parameter, JdbcType jdbcType) {
        TypeHandler<?> handler;
        if (parameter == null) {
            handler = OBJECT_TYPE_HANDLER;
        } else {
            handler = config.getTypeHandlerRegistry().getTypeHandler(parameter.getClass(), jdbcType);
            // check if handler is null (issue #270)
            if (handler == null || handler instanceof UnknownTypeHandler) {
                handler = OBJECT_TYPE_HANDLER;
            }
        }
        return handler;
    }
    @Override
    public Object getNullableResult(ResultSet rs, String columnName)
            throws SQLException {
        throw new UnfinishedFunctionException();
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException {
        throw new UnfinishedFunctionException();
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        return cs.getObject(columnIndex);
    }
}
