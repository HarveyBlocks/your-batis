package org.harvey.batis.mapping.sqlsource;

import org.harvey.batis.builder.SqlSourceBuilder;
import org.harvey.batis.config.Configuration;
import org.harvey.batis.mapping.BoundSql;
import org.harvey.batis.scripting.xml.DynamicContext;
import org.harvey.batis.scripting.xml.SqlNode;

import java.util.HashMap;

/**
 * 文本SqlSource, 可能有不同的需要解析的内容, 交给内部保存的{@link #sqlSource}, 其将真正执行解析逻辑
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-11 23:59
 */
public class RawSqlSource implements SqlSource {
    private final SqlSource sqlSource;

    public RawSqlSource(Configuration configuration, SqlNode rootSqlNode, Class<?> parameterType) {
        this(configuration, getSql(configuration, rootSqlNode), parameterType);
    }

    public RawSqlSource(Configuration configuration, String sql, Class<?> parameterType) {
        SqlSourceBuilder sqlSourceBuilder = new SqlSourceBuilder(configuration);
        Class<?> clazz = parameterType == null ? Object.class : parameterType;
        sqlSource = sqlSourceBuilder.build(sql, clazz, new HashMap<>());
    }

    private static String getSql(Configuration configuration, SqlNode rootSqlNode) {
        DynamicContext context = new DynamicContext(configuration, null);
        rootSqlNode.apply(context);
        return context.getSql();
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return sqlSource.getBoundSql(parameterObject);
    }
}
