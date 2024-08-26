package org.harvey.batis.executor.statement;

import org.harvey.batis.cursor.Cursor;
import org.harvey.batis.exception.UnfinishedFunctionException;
import org.harvey.batis.executor.Executor;
import org.harvey.batis.executor.key.generator.Jdbc3KeyGenerator;
import org.harvey.batis.executor.key.generator.KeyGenerator;
import org.harvey.batis.executor.result.ResultHandler;
import org.harvey.batis.executor.result.ResultSetHandler;
import org.harvey.batis.mapping.BoundSql;
import org.harvey.batis.mapping.MappedStatement;
import org.harvey.batis.session.RowBounds;
import org.harvey.batis.util.enums.ResultSetType;

import java.sql.*;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-20 00:24
 */
public class PreparedStatementHandler extends BaseStatementHandler {

    protected PreparedStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler<?> resultHandler, BoundSql boundSql) {
        super(executor, mappedStatement, parameterObject, rowBounds, resultHandler, boundSql);
    }

    @Override
    protected Statement instantiateStatement(Connection connection) throws SQLException {
        String sql = boundSql.getSql();
        if (mappedStatement.getKeyGenerator() instanceof Jdbc3KeyGenerator) {
            // 启用了KeyGenerator的话
            String[] keyColumnNames = mappedStatement.getKeyColumns();
            if (keyColumnNames == null) {
                return connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            } else {
                return connection.prepareStatement(sql, keyColumnNames);
            }
        }
        if (mappedStatement.getResultSetType() == ResultSetType.DEFAULT) {
            return connection.prepareStatement(sql);
        } else {
            return connection.prepareStatement(sql, mappedStatement.getResultSetType().value(), ResultSet.CONCUR_READ_ONLY);
        }
    }

    @Override
    public void parameterize(Statement statement) throws SQLException {
        parameterHandler.setParameters((PreparedStatement) statement);
    }

    @Override
    public void batch(Statement statement) throws SQLException {
        throw new UnfinishedFunctionException();
    }

    @Override
    public int update(Statement statement) throws SQLException {
        PreparedStatement ps = (PreparedStatement) statement;
        ps.execute();
        int rows = ps.getUpdateCount();
        Object parameterObject = boundSql.getParameterObject();
        KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
        keyGenerator.processAfter(executor, mappedStatement, ps, parameterObject);
        return rows;
    }

    /**
     * 真正执行查询的地方, 并将结果解析交给{@link #resultSetHandler}
     *
     * @see PreparedStatement#execute()
     * @see ResultSetHandler#handleResultSets(Statement)
     */
    @Override
    public <E> List<E> query(Statement statement, ResultHandler<?> resultHandler) throws SQLException {
        PreparedStatement ps = (PreparedStatement) statement;
        ps.execute();
        return resultSetHandler.handleResultSets(ps);
    }

    @Override
    public <E> Cursor<E> queryCursor(Statement statement) throws SQLException {
        throw new UnfinishedFunctionException();
    }
}