package org.harvey.batis.executor.key.generator;

import org.harvey.batis.exception.UnfinishedFunctionException;
import org.harvey.batis.executor.Executor;
import org.harvey.batis.mapping.MappedStatement;

import java.sql.Statement;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-21 00:56
 */
public class SelectKeyGenerator implements KeyGenerator{
    public static final String SELECT_KEY_SUFFIX = "!selectKey";
    @Override
    public void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        throw new UnfinishedFunctionException();
    }

    @Override
    public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        throw new UnfinishedFunctionException();
    }

}
