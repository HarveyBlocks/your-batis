package org.harvey.batis.io.log.jdbc;

import lombok.Getter;
import org.harvey.batis.exception.UnfinishedFunctionException;
import org.harvey.batis.io.log.Log;
import org.harvey.batis.util.ReflectionExceptionUnwrappedMaker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * TODO
 * 为名为PreparedStatement的方法代理添加日志记录。
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-20 00:03
 */
@Getter
public final class PreparedStatementLogger extends BaseJdbcLogger implements InvocationHandler {

    private final PreparedStatement statement;

    private PreparedStatementLogger(PreparedStatement stmt, Log statementLog, int queryStack) {
        super(statementLog, queryStack);
        this.statement = stmt;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
        try {
            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, params);
            }
            String methodName = method.getName();
            if (SET_METHODS.contains(methodName)) {
                this.preSetMethods(method, params);
                return method.invoke(statement, params);
            } else if (EXECUTE_METHODS.contains(methodName)) {
                // 执行前记录日志, 输出Parameter信息
                debugIfEnable("Parameters: " + super.getParameterValueString(), true);
                super.clearColumnInfo();
                if (!"executeQuery".equals(methodName)) {
                    return method.invoke(statement, params);
                }
                ResultSet rs = (ResultSet) method.invoke(statement, params);
                // 创建新的ResultSet代理
                return rs == null ? null : ResultSetLogger.newInstance(rs, statementLog, queryStack);
            } else if ("getResultSet".equals(methodName)) {
                ResultSet rs = (ResultSet) method.invoke(statement, params);
                // 创建新的ResultSet代理
                return rs == null ? null : ResultSetLogger.newInstance(rs, statementLog, queryStack);
            } else if ("getUpdateCount".equals(methodName)) {
                int updateCount = (Integer) method.invoke(statement, params);
                if (updateCount != -1) {
                    debug("   Updates: " + updateCount, false);
                }
                return updateCount;
            } else {
                return method.invoke(statement, params);
            }
        } catch (Throwable t) {
            throw ReflectionExceptionUnwrappedMaker.unwrapThrowable(t);
        }
    }

    /**
     * @param method 用于判断是否是setNull方法以作不同工作
     * @param params 用于转成key-value
     * @see BaseJdbcLogger#setColumn(Object, Object)
     */
    private void preSetMethods(Method method, Object[] params) {
        Object key = params[0];
        Object value = "setNull".equals(method.getName()) ? null : params[1];
        super.setColumn(key, value);
    }

    /**
     * @see PreparedStatementLogger
     */
    public static PreparedStatement newInstance(PreparedStatement stmt, Log statementLog, int queryStack) {
        InvocationHandler handler = new PreparedStatementLogger(stmt, statementLog, queryStack);
        ClassLoader cl = PreparedStatement.class.getClassLoader();
        return (PreparedStatement) Proxy.newProxyInstance(cl, new Class[]{PreparedStatement.class, CallableStatement.class}, handler);
    }


}