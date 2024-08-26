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
 * @date 2024-08-21 00:52
 */
public class Jdbc3KeyGenerator implements KeyGenerator {
    public static final KeyGenerator INSTANCE = new Jdbc3KeyGenerator();

    @Override
    public void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        throw new UnfinishedFunctionException();
    }

    @Override
    public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        throw new UnfinishedFunctionException();
    }
}
