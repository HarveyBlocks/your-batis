package org.harvey.batis.session;

import org.harvey.batis.config.Configuration;
import org.harvey.batis.enums.ExecutorType;
import org.harvey.batis.enums.TransactionIsolationLevel;

import java.sql.Connection;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-02 12:32
 */
public interface SqlSessionFactory {

    SqlSession openSession();

    SqlSession openSession(boolean autoCommit);

    SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level);

    SqlSession openSession(ExecutorType execType, boolean autoCommit);

    SqlSession openSession(Connection connection);

    SqlSession openSession(ExecutorType execType);

    SqlSession openSession(TransactionIsolationLevel level);

    SqlSession openSession(ExecutorType execType, Connection connection);

    Configuration getConfiguration();
}
