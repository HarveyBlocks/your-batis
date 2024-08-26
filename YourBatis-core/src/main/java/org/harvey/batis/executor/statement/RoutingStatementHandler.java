package org.harvey.batis.executor.statement;

import org.harvey.batis.cursor.Cursor;
import org.harvey.batis.exception.UnfinishedFunctionException;
import org.harvey.batis.exception.executor.ExecutorException;
import org.harvey.batis.executor.Executor;
import org.harvey.batis.executor.param.ParameterHandler;
import org.harvey.batis.executor.result.ResultHandler;
import org.harvey.batis.mapping.BoundSql;
import org.harvey.batis.mapping.MappedStatement;
import org.harvey.batis.session.RowBounds;
import org.harvey.batis.util.enums.StatementType;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 依据{@link MappedStatement#getStatementType()}的{@link StatementType}
 * 路由到合适的{@link StatementHandler}
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-20 00:19
 */
public class RoutingStatementHandler implements StatementHandler {

    private final StatementHandler delegate;

    /**
     * 参数将全部用来构造适合的{@link StatementHandler}
     *
     * @param ms 其中的{@link StatementType}将给出路由的依据
     */
    public RoutingStatementHandler(Executor executor, MappedStatement ms,
                                   Object parameter, RowBounds rowBounds,
                                   ResultHandler<?> resultHandler, BoundSql boundSql) {

        switch (ms.getStatementType()) {
            case STATEMENT:
                delegate = null;
                // delegate = new SimpleStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
                throw new UnfinishedFunctionException(StatementType.STATEMENT);
                // break;
            case PREPARED:
                delegate = new PreparedStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
                break;
            case CALLABLE:
                delegate = null;
                // delegate = new CallableStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
                throw new UnfinishedFunctionException(StatementType.CALLABLE);
                // break;
            default:
                throw new ExecutorException("Unknown statement type: " + ms.getStatementType());
        }

    }

    @Override
    public Statement prepare(Connection connection, Integer transactionTimeout) throws SQLException {
        return delegate.prepare(connection, transactionTimeout);
    }

    @Override
    public void parameterize(Statement statement) throws SQLException {
        delegate.parameterize(statement);
    }

    @Override
    public void batch(Statement statement) throws SQLException {
        delegate.batch(statement);
    }

    @Override
    public int update(Statement statement) throws SQLException {
        return delegate.update(statement);
    }

    @Override
    public <E> List<E> query(Statement statement, ResultHandler<?> resultHandler) throws SQLException {
        return delegate.query(statement, resultHandler);
    }

    @Override
    public <E> Cursor<E> queryCursor(Statement statement) throws SQLException {
        return delegate.queryCursor(statement);
    }

    @Override
    public BoundSql getBoundSql() {
        return delegate.getBoundSql();
    }

    @Override
    public ParameterHandler getParameterHandler() {
        return delegate.getParameterHandler();
    }
}
