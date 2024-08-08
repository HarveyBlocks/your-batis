package org.harvey.batis.session;

import org.harvey.batis.config.Configuration;
import org.harvey.batis.enums.ExecutorType;
import org.harvey.batis.enums.TransactionIsolationLevel;
import org.harvey.batis.exception.UnfinishedFunctionException;

import java.sql.Connection;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-02 12:32
 */
public class DefaultSqlSessionFactory implements SqlSessionFactory {
    private final Configuration configuration;

    public DefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public SqlSession openSession() {
        return openSessionFromDataSource(configuration.getDefaultExecutorType(), null, false);
    }

    @Override
    public SqlSession openSession(boolean autoCommit) {
        return openSessionFromDataSource(configuration.getDefaultExecutorType(), null, autoCommit);
    }

    @Override
    public SqlSession openSession(ExecutorType execType) {
        return openSessionFromDataSource(execType, null, false);
    }

    @Override
    public SqlSession openSession(TransactionIsolationLevel level) {
        return openSessionFromDataSource(configuration.getDefaultExecutorType(), level, false);
    }

    @Override
    public SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level) {
        return openSessionFromDataSource(execType, level, false);
    }

    @Override
    public SqlSession openSession(ExecutorType execType, boolean autoCommit) {
        return openSessionFromDataSource(execType, null, autoCommit);
    }

    @Override
    public SqlSession openSession(Connection connection) {
        return this.openSessionFromConnection(configuration.getDefaultExecutorType(), connection);
    }


    @Override
    public SqlSession openSession(ExecutorType execType, Connection connection) {
        return this.openSessionFromConnection(execType, connection);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * TODO
     */
    private SqlSession openSessionFromDataSource(ExecutorType execType, TransactionIsolationLevel level, boolean autoCommit) {
        throw new UnfinishedFunctionException(execType, level, autoCommit);
    }

    /**
     * TODO
     */
    private SqlSession openSessionFromConnection(ExecutorType execType, Connection connection) {
        throw new UnfinishedFunctionException(execType, connection);
    }
}
