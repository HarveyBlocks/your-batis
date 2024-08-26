package org.harvey.batis.session;

import org.harvey.batis.config.Configuration;
import org.harvey.batis.cursor.Cursor;
import org.harvey.batis.exception.ExceptionFactory;
import org.harvey.batis.exception.UnfinishedFunctionException;
import org.harvey.batis.executor.BatchResult;
import org.harvey.batis.executor.Executor;
import org.harvey.batis.mapping.MappedStatement;
import org.harvey.batis.reflection.ParamNameResolver;
import org.harvey.batis.util.ErrorContext;

import java.io.IOException;
import java.util.List;

/**
 * TODO
 * <b>线程不安全!</b>
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-02 12:33
 */
public class DefaultSqlSession implements SqlSession {
    private final Configuration configuration;
    private final Executor executor;

    private final boolean autoCommit;
    /**
     * 写操作时, 可能引起脏读, 故设置为true
     */
    private boolean dirty;
    private List<Cursor<?>> cursorList;

    public DefaultSqlSession(Configuration configuration, Executor executor, boolean autoCommit) {
        this.configuration = configuration;
        this.executor = executor;
        this.dirty = false;
        this.autoCommit = autoCommit;
    }

    public DefaultSqlSession(Configuration configuration, Executor executor) {
        this(configuration, executor, false);
    }

    /**
     * <pre>{@code dirty=false}</pre>
     *
     * @see Executor#close(boolean)
     * @see #closeCursors()
     */
    @Override
    public void close() {
        try {
            executor.close(this.isCommitOrRollbackRequired(false));
            this.closeCursors();
            dirty = false;
        } finally {
            ErrorContext.instance().reset();
        }
    }

    /**
     * @param force 若为ture, 则强制关闭返回true/ 若为false, 则看
     *              <pre>{@code  !autoCommit && dirty; }</pre>
     * @see #autoCommit
     * @see #dirty
     */
    private boolean isCommitOrRollbackRequired(boolean force) {
        return (!autoCommit && dirty) || force;
    }

    /**
     * 关闭{@link #cursorList}, 并{@link List#clear()}
     */
    private void closeCursors() {
        if (cursorList == null || cursorList.isEmpty()) {
            return;
        }
        for (Cursor<?> cursor : cursorList) {
            try {
                cursor.close();
            } catch (IOException e) {
                throw ExceptionFactory.wrapException("Error closing cursor.  Cause: " + e, e);
            }
        }
        cursorList.clear();
    }

    @Override
    public <T> T getMapper(Class<T> type) {
        return configuration.getMapper(type, this);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public List<BatchResult> flushStatements() {
        try {
            return executor.flushStatements();
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error flushing statements.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        throw new UnfinishedFunctionException();
    }

    @Override
    public <E> List<E> selectList(String statement, Object parameter) {
        return this.selectList(statement, parameter, RowBounds.DEFAULT);
    }

    @Override
    public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
        try {
            MappedStatement ms = configuration.getMappedStatement(statement);
            return executor.query(ms,
                    this.wrapCollection(parameter), // 目的不明
                    rowBounds, Executor.NO_RESULT_HANDLER);
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error querying database.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    /**
     * @see ParamNameResolver#wrapToMapIfCollection(Object, String)
     */
    private Object wrapCollection(final Object object) {
        return ParamNameResolver.wrapToMapIfCollection(object, null);
    }

    @Override
    public int insert(String statement) {
        return insert(statement, null);
    }

    @Override
    public int insert(String statement, Object parameter) {
        return update(statement, parameter);
    }


    @Override
    public int delete(String statement) {
        return update(statement, null);
    }

    @Override
    public int delete(String statement, Object parameter) {
        return update(statement, parameter);
    }

    @Override
    public int update(String statement) {
        return update(statement, null);
    }

    @Override
    public int update(String statement, Object parameter) {
        try {
            dirty = true;
            MappedStatement ms = configuration.getMappedStatement(statement);
            return executor.update(ms, wrapCollection(parameter));
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error updating database.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    @Override
    public void commit() {
        commit(false);
    }

    @Override
    public void commit(boolean force) {
        try {
            executor.commit(isCommitOrRollbackRequired(force));
            dirty = false;
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error committing transaction.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    @Override
    public void rollback() {
        rollback(false);
    }

    @Override
    public void rollback(boolean force) {
        try {
            executor.rollback(isCommitOrRollbackRequired(force));
            dirty = false;
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error rolling back transaction.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }
}
