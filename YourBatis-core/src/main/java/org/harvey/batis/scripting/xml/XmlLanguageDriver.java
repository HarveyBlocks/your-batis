package org.harvey.batis.scripting.xml;

import org.harvey.batis.config.Configuration;
import org.harvey.batis.executor.param.DefaultParameterHandler;
import org.harvey.batis.executor.param.ParameterHandler;
import org.harvey.batis.mapping.BoundSql;
import org.harvey.batis.mapping.MappedStatement;
import org.harvey.batis.mapping.sqlsource.SqlSource;
import org.harvey.batis.parsing.XNode;
import org.harvey.batis.scripting.LanguageDriver;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-11 17:53
 */
public class XmlLanguageDriver implements LanguageDriver {
    @Override
    public SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType) {
        XmlScriptBuilder builder = new XmlScriptBuilder(configuration, script, parameterType);
        return builder.parseScriptNode();
    }

    /**
     * {@inheritDoc}
     *
     * @see DefaultParameterHandler
     */
    @Override
    public ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        return new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
    }
}
