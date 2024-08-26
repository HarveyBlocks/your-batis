package org.harvey.batis.executor.param;

import org.harvey.batis.config.Configuration;
import org.harvey.batis.exception.UnfinishedFunctionException;
import org.harvey.batis.exception.type.TypeException;
import org.harvey.batis.mapping.BoundSql;
import org.harvey.batis.mapping.MappedStatement;
import org.harvey.batis.mapping.ParameterMapping;
import org.harvey.batis.reflection.MetaObject;
import org.harvey.batis.util.ErrorContext;
import org.harvey.batis.util.enums.JdbcType;
import org.harvey.batis.util.enums.ParameterMode;
import org.harvey.batis.util.type.TypeHandler;
import org.harvey.batis.util.type.TypeHandlerRegistry;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-20 22:34
 * @see TypeHandlerRegistry
 * @see MappedStatement
 */
public class DefaultParameterHandler implements ParameterHandler {

    private final Configuration configuration;
    private final MappedStatement mappedStatement;
    private final TypeHandlerRegistry typeHandlerRegistry;
    private final Object parameterObject;
    private final BoundSql boundSql;

    public DefaultParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        this.mappedStatement = mappedStatement;
        this.configuration = mappedStatement.getConfiguration();
        this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        this.parameterObject = parameterObject;
        this.boundSql = boundSql;
    }

    @Override
    public Object getParameterObject() {
        throw new UnfinishedFunctionException();
    }

    @Override
    public void setParameters(PreparedStatement ps) throws SQLException {
        ErrorContext.instance().setActivity("setting parameters").setObject(mappedStatement.getParameterMap().getId());
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings == null) {
            return;
        }
        for (int i = 0; i < parameterMappings.size(); i++) {
            // 遍历parameterMappings
            ParameterMapping parameterMapping = parameterMappings.get(i);
            if (parameterMapping.getMode() == ParameterMode.OUT) {
                continue;
            }
            // 具有ParameterMode.IN的情况
            // 接下来决定要填充到Statement里的value的值
            String propertyName = parameterMapping.getProperty();
            // 获取TypeHandler用于向ps写入参数
            TypeHandler typeHandler = parameterMapping.getTypeHandler();
            Object value = this.getPorpertyFromParameterObject(propertyName);
            // 获取JdbcType
            // typeHandler写入参数需要JdbcType
            JdbcType jdbcType = parameterMapping.getJdbcType();
            if (value == null && jdbcType == null) {
                jdbcType = configuration.getJdbcTypeForNull();
            }
            try {
                // 交给TypeHandler填充参数(因为不同类型, 填充参数的逻辑不一样)
                typeHandler.setParameter(ps, i + 1, value, jdbcType);
            } catch (TypeException | SQLException e) {
                throw new TypeException("Could not set parameters for mapping: " + parameterMapping + ". Cause: " + e, e);
            }
        }
    }

    /**
     * 依据propertyName的不同情况, 从boundSql/typeHandlerRegistry/parameterObject取出数据
     *
     * @see #getPorpertyFromParameterObject(String, BoundSql, TypeHandlerRegistry, Configuration, Object)
     */
    public Object getPorpertyFromParameterObject(String propertyName) {
        return DefaultParameterHandler.getPorpertyFromParameterObject(propertyName, boundSql, typeHandlerRegistry, configuration, parameterObject);
    }

    /**
     * 依据propertyName的不同情况, 从boundSql/typeHandlerRegistry/parameterObject取出数据
     *
     * @see BoundSql#hasAdditionalParameter(String) 情况一
     * @see Object propertyName==null的情况
     * @see TypeHandlerRegistry#hasTypeHandler(Class)  情况三
     * @see Configuration#newMetaObject(Object)   else
     */
    public static Object getPorpertyFromParameterObject(
            String propertyName, BoundSql boundSql, TypeHandlerRegistry typeHandlerRegistry,
            Configuration configuration, Object parameterObject) {
        if (boundSql.hasAdditionalParameter(propertyName)) {
            // propertyName特殊的处理, 如果给propertyName配置了AdditionalParameter
            return boundSql.getAdditionalParameter(propertyName);
        } else if (parameterObject == null) {
            return null;
        } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
            // parameterObject可能是Bean, 如果是Bean, 就有可能是TypeHandler
            // 如果是TypeHandler, 就将value设置为parameterObject
            return parameterObject;
        } else {
            // 普通的类型, 通过反射获取值
            MetaObject metaObject = configuration.newMetaObject(parameterObject);
            return metaObject.getValue(propertyName);
        }
    }
}
