package org.harvey.batis.mapping.sqlsource;


import org.harvey.batis.mapping.BoundSql;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-11 17:43
 */
public interface SqlSource {
    BoundSql getBoundSql(Object parameterObject);
}
