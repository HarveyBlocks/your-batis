package org.harvey.batis.io.log.jdbc;

import lombok.Getter;
import org.harvey.batis.exception.UnfinishedFunctionException;
import org.harvey.batis.io.log.Log;
import org.harvey.batis.util.ReflectionExceptionUnwrappedMaker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-20 00:04
 */
@Getter
public final class StatementLogger extends BaseJdbcLogger implements InvocationHandler {

    private final Statement statement;

    private StatementLogger(Statement stmt, Log statementLog, int queryStack) {
        super(statementLog, queryStack);
        this.statement = stmt;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
        try {
            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, params);
            }
            if (EXECUTE_METHODS.contains(method.getName())) {
                throw new UnfinishedFunctionException();
            } else if ("getResultSet".equals(method.getName())) {
                throw new UnfinishedFunctionException();
            } else {
                return method.invoke(statement, params);
            }
        } catch (Throwable t) {
            throw ReflectionExceptionUnwrappedMaker.unwrapThrowable(t);
        }
    }

    public static Statement newInstance(Statement stmt, Log statementLog, int queryStack) {
        InvocationHandler handler = new StatementLogger(stmt, statementLog, queryStack);
        ClassLoader cl = Statement.class.getClassLoader();
        return (Statement) Proxy.newProxyInstance(cl, new Class[]{Statement.class}, handler);
    }

}
