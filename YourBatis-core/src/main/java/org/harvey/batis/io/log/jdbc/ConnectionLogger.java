package org.harvey.batis.io.log.jdbc;

import lombok.Getter;
import org.harvey.batis.io.log.Log;
import org.harvey.batis.util.ReflectionExceptionUnwrappedMaker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * TODO
 * Connection proxy to add logging.
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-19 23:45
 */
@Getter
public final class ConnectionLogger extends BaseJdbcLogger implements InvocationHandler {

    private final Connection connection;

    private ConnectionLogger(Connection conn, Log statementLog, int queryStack) {
        super(statementLog, queryStack);
        this.connection = conn;
    }

    /**
     * 对于一般的方法, 正常执行;
     * 对于方法名为prepareStatement和createStatement的方法, 进行增强
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] params)
            throws Throwable {
        try {
            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, params);
            }
            if ("prepareStatement".equals(method.getName()) || "prepareCall".equals(method.getName())) {
                // 输出SQL语句日志(还未填充参数)
                this.debugIfEnable(" Preparing: " + BaseJdbcLogger.removeExtraWhitespace((String) params[0]), true);
                PreparedStatement stmt = (PreparedStatement) method.invoke(connection, params);
                // 对stmt做日志增强
                return PreparedStatementLogger.newInstance(stmt, statementLog, queryStack);
            } else if ("createStatement".equals(method.getName())) {
                Statement stmt = (Statement) method.invoke(connection, params);
                // 对stmt做日志增强
                return StatementLogger.newInstance(stmt, statementLog, queryStack);
            } else {
                return method.invoke(connection, params);
            }
        } catch (Throwable t) {
            throw ReflectionExceptionUnwrappedMaker.unwrapThrowable(t);
        }
    }

    /**
     * Creates a logging version of a connection.
     * 创建连接的日志记录版本
     *
     * @param conn         原生的连接
     * @param statementLog 原来的日志
     * @param queryStack   查询栈深度
     * @return 代理的连接, 转换了日志
     */
    public static Connection newInstance(Connection conn, Log statementLog, int queryStack) {
        // 创建本类实例作为增强
        InvocationHandler handler = new ConnectionLogger(conn, statementLog, queryStack);
        // 原生的连接的ClassLoader作为类加载器
        ClassLoader cl = Connection.class.getClassLoader();
        return (Connection) Proxy.newProxyInstance(cl,
                new Class[]{Connection.class},// 让代理成为原生连接的子实现
                handler);
    }

}
