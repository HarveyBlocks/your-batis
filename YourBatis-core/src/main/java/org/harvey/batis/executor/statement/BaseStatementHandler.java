package org.harvey.batis.executor.statement;

import org.harvey.batis.config.Configuration;
import org.harvey.batis.exception.UnfinishedFunctionException;
import org.harvey.batis.exception.executor.ExecutorException;
import org.harvey.batis.executor.Executor;
import org.harvey.batis.executor.key.generator.KeyGenerator;
import org.harvey.batis.executor.param.ParameterHandler;
import org.harvey.batis.executor.result.ResultHandler;
import org.harvey.batis.executor.result.ResultSetHandler;
import org.harvey.batis.mapping.BoundSql;
import org.harvey.batis.mapping.MappedStatement;
import org.harvey.batis.reflection.factory.ObjectFactory;
import org.harvey.batis.session.RowBounds;
import org.harvey.batis.util.ErrorContext;
import org.harvey.batis.util.type.TypeHandlerRegistry;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-20 00:24
 */
public abstract class BaseStatementHandler implements StatementHandler {
    protected final Configuration configuration;
    protected final ObjectFactory objectFactory;
    protected final TypeHandlerRegistry typeHandlerRegistry;

    protected final Executor executor;
    protected final MappedStatement mappedStatement;
    protected final RowBounds rowBounds;
    protected BoundSql boundSql;
    protected final ResultSetHandler resultSetHandler;
    protected final ParameterHandler parameterHandler;

    protected BaseStatementHandler(
            Executor executor, MappedStatement mappedStatement,
            Object parameterObject, RowBounds rowBounds,
            ResultHandler<?> resultHandler, BoundSql boundSql) {
        this.configuration = mappedStatement.getConfiguration();
        this.executor = executor;
        this.mappedStatement = mappedStatement;
        this.rowBounds = rowBounds;

        this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        this.objectFactory = configuration.getObjectFactory();

        if (boundSql != null) {
            this.boundSql = boundSql;
        } else {
            // 在计算mappedStatement之前获取generateKeys
            this.generateKeys(parameterObject);
            this.boundSql = mappedStatement.getBoundSql(parameterObject);
        }

        this.parameterHandler = configuration
                .newParameterHandler(mappedStatement, parameterObject, this.boundSql);
        this.resultSetHandler = configuration
                .newResultSetHandler(executor, mappedStatement, rowBounds, parameterHandler, resultHandler, boundSql);
    }

    /**
     * @param parameter 依据参数创建出{@link KeyGenerator}, <br>
     *                  然后用keyGenerator实例{@link KeyGenerator#processBefore(Executor, MappedStatement, Statement, Object)}
     */
    protected void generateKeys(Object parameter) {
        KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
        ErrorContext.instance().store();
        keyGenerator.processBefore(executor, mappedStatement, null, parameter);
        ErrorContext.instance().recall();
    }

    @Override
    public BoundSql getBoundSql() {
        return boundSql;
    }

    @Override
    public ParameterHandler getParameterHandler() {
        return parameterHandler;
    }

    @Override
    public Statement prepare(Connection connection, Integer transactionTimeout) throws SQLException {
        ErrorContext.instance().setSql(boundSql.getSql());
        Statement statement = null;
        try {
            statement = this.instantiateStatement(connection);
            // 设置参数
            this.setStatementTimeout(statement, transactionTimeout);
            this.setFetchSize(statement);
            return statement;
        } catch (SQLException e) {
            // 认为是原生的JDBC出现了问题
            this.closeStatement(statement);
            throw e;
        } catch (Exception e) {
            // 认为是解析出现了问题
            this.closeStatement(statement);
            throw new ExecutorException("Error preparing statement.  Cause: " + e, e);
        }
    }

    /**
     * 实例化Statement
     */
    protected abstract Statement instantiateStatement(Connection connection) throws SQLException;

    /**
     * 从{@link #mappedStatement}中取出查询用Timeout存入stmt<br>
     * 取不出? 就从{@link #configuration}中取出默认的查询用Timeout存入stmt
     *
     * @param stmt 需要被存入查询用Timeout的对象
     * @see StatementUtil#applyTransactionTimeout(Statement, Integer, Integer)
     * @see Statement#setQueryTimeout(int)
     */
    protected void setStatementTimeout(Statement stmt, Integer transactionTimeout) throws SQLException {
        Integer queryTimeout = null;
        if (mappedStatement.getTimeout() != null) {
            queryTimeout = mappedStatement.getTimeout();
        } else if (configuration.getDefaultStatementTimeout() != null) {
            queryTimeout = configuration.getDefaultStatementTimeout();
        }
        if (queryTimeout != null) {
            stmt.setQueryTimeout(queryTimeout);
        }
        StatementUtil.applyTransactionTimeout(stmt, queryTimeout, transactionTimeout);
    }

    /**
     * 从{@link #mappedStatement}中取出FetchSize存入stmt<br>
     * 取不出? 就从{@link #configuration}中取出默认的FetchSize存入stmt
     *
     * @param stmt 需要被存入FetchSize的对象
     * @see Statement#setFetchSize(int)
     */
    protected void setFetchSize(Statement stmt) throws SQLException {
        Integer fetchSize = mappedStatement.getFetchSize();
        if (fetchSize != null) {
            stmt.setFetchSize(fetchSize);
            return;
        }
        Integer defaultFetchSize = configuration.getDefaultFetchSize();
        if (defaultFetchSize != null) {
            stmt.setFetchSize(defaultFetchSize);
        }
    }

    /**
     * 关闭statement, 忽略异常
     *
     * @param statement 需要被关闭的statement
     */
    protected void closeStatement(Statement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException ignore) {
        }
    }


}
