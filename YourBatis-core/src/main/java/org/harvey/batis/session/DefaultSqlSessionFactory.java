package org.harvey.batis.session;

import org.harvey.batis.config.Configuration;
import org.harvey.batis.exception.ExceptionFactory;
import org.harvey.batis.exception.UnfinishedFunctionException;
import org.harvey.batis.executor.Executor;
import org.harvey.batis.mapping.Environment;
import org.harvey.batis.transaction.Transaction;
import org.harvey.batis.transaction.TransactionFactory;
import org.harvey.batis.util.ErrorContext;
import org.harvey.batis.util.enums.ExecutorType;
import org.harvey.batis.util.enums.TransactionIsolationLevel;

import java.sql.Connection;
import java.sql.SQLException;

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
        Transaction tx = null;
        try {
            final Environment environment = configuration.getEnvironment();
            final TransactionFactory transactionFactory = this.getTransactionFactoryFromEnvironment(environment);
            tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);
            final Executor executor = configuration.newExecutor(tx, execType);
            return new DefaultSqlSession(configuration, executor, autoCommit);
        } catch (Exception e) {
            this.closeTransaction(tx); // 可能获取了一个连接后发生异常，所以close
            throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    /**
     * TODO
     */
    private SqlSession openSessionFromConnection(ExecutorType execType, Connection connection) {
        throw new UnfinishedFunctionException(execType, connection);
    }

    private TransactionFactory getTransactionFactoryFromEnvironment(Environment environment) {
        if (environment == null || environment.getTransactionFactory() == null) {
            throw new UnfinishedFunctionException();
            // return new ManagedTransactionFactory();
        }
        return environment.getTransactionFactory();
    }

    private void closeTransaction(Transaction tx) {
        if (tx != null) {
            try {
                tx.close();
            } catch (SQLException ignore) {
                // 故意忽略。选则上一个错误
            }
        }
    }
}
