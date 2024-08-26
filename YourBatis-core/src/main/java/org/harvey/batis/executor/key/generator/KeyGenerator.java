package org.harvey.batis.executor.key.generator;

import org.harvey.batis.executor.Executor;
import org.harvey.batis.mapping.MappedStatement;

import java.sql.Statement;

/**
 * 在写入一条记录之后, 是否将ID注入JavaBean
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-21 00:51
 */
public interface KeyGenerator {
    void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter);

    void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter);
}
