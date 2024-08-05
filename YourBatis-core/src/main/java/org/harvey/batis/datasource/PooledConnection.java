package org.harvey.batis.datasource;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.harvey.batis.util.ReflectionExceptionUnwrappedMaker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 对Connect连接的一个封装
 * 🤔 : 这是不是滴哦{@link Connection}的代理?
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-01 12:47
 */
public class PooledConnection implements InvocationHandler {

    /**
     * 连接是否有效, true表示有效
     */
    private boolean valid;
    private static final String CLOSE_METHOD_NAME = "close";
    private static final Class<?>[] INTERFACES = new Class<?>[]{Connection.class};

    private final int hashCode;
    /**
     * realConnection 来自于的 dataSource
     */
    private final PooledDataSource dataSource;
    /**
     * 被池化的连接
     */
    @Getter
    private final Connection realConnection;
    /**
     * {@link #realConnection}的proxy
     */
    @Getter
    private final Connection proxyConnection;
    /**
     * 连接被检查的时间
     */
    @Setter
    @Getter
    private long checkoutTimestamp;
    /**
     * 连接创建的时间
     */
    @Setter
    @Getter
    private long createdTimestamp;
    /**
     * 上次使用连接的时间
     */
    @Setter
    @Getter
    private long lastUsedTimestamp;
    /**
     * 由url, username, password共同决定
     */
    @Getter
    @Setter
    private ConnectionType connectionTypeCode;

    /**
     * 组装连接类型码
     */
    @Data
    public static class ConnectionType {
        private final int code;

        public ConnectionType(int code) {
            this.code = code;
        }

        public ConnectionType(String url, String username, String password) {
            this.code = this.assembleConnectionTypeCode(url, username, password);
        }

        public ConnectionType(UnpooledDataSource dataSource) {
            this.code = this.assembleConnectionTypeCode(dataSource);
        }

        /**
         * 获取其DataSource中的配置, 组装连接类型码
         *
         * @param dataSource 提供url, username, password
         * @see #assembleConnectionTypeCode(String, String, String)
         */
        private int assembleConnectionTypeCode(UnpooledDataSource dataSource) {
            return dataSource == null ? 0 : this.assembleConnectionTypeCode(
                    dataSource.getUrl(),
                    dataSource.getUsername(),
                    dataSource.getPassword());
        }

        /**
         * 组装连接类型码
         */
        private int assembleConnectionTypeCode(String url, String username, String password) {
            if (url == null || username == null || password == null) {
                return 0;
            } else {
                return (url + username + password).hashCode();
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof ConnectionType)) {
                return false;
            }
            ConnectionType connectionType = (ConnectionType) obj;
            return connectionType.code == this.code;
        }

        @Override
        public int hashCode() {
            return this.code;
        }
    }

    /**
     * @param connection {@link #realConnection}
     * @param dataSource {@link #dataSource}
     */
    public PooledConnection(Connection connection, PooledDataSource dataSource) {
        this.hashCode = connection.hashCode();
        this.realConnection = connection;
        this.dataSource = dataSource;
        this.createdTimestamp = System.currentTimeMillis();
        this.lastUsedTimestamp = System.currentTimeMillis();
        this.valid = true;
        this.proxyConnection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), INTERFACES, this);
    }


    /**
     * 无效化
     */
    public void invalidate() {
        valid = false;
    }

    /**
     * @return 包装可用且连接存在(不为null)且ping得通, 返回true
     */
    public boolean isValid() {
        return valid && realConnection != null && dataSource.pingConnection(this);
    }

    /**
     * @return realConnection的Hashcode, 如果realConnection为null则返回0
     */
    public int getRealHashCode() {
        return realConnection == null ? 0 : realConnection.hashCode();
    }

    /**
     * @return 获取自上次使用以来经过的时间
     */
    public long getTimeElapsedSinceLastUse() {
        return System.currentTimeMillis() - lastUsedTimestamp;
    }


    /**
     * @return 从次连接创建以来过去的时间间隔
     */
    public long getAge() {
        return System.currentTimeMillis() - createdTimestamp;
    }


    /**
     * @return 上次检查该连接过去的时间间隔
     */
    public long getCheckoutTime() {
        return System.currentTimeMillis() - checkoutTimestamp;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    /**
     * @throws SQLException 如果当前连接不可用就抛出异常
     */
    private void checkConnection() throws SQLException {
        if (!valid) {
            throw new SQLException("Error accessing PooledConnection. Connection is invalid.");
        }
    }

    /**
     * 代理执行{@link #realConnection}中的方法
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if (CLOSE_METHOD_NAME.equals(methodName)) {
            // 特别的, 对于close方法
            // 当连接关闭时, 将本连接放入空闲ConnectionList
            dataSource.pushConnection(this);
            return null;
        }
        try {
            if (!Object.class.equals(method.getDeclaringClass())) {
                // 如果执行的是Object里自带的方法
                // issue #579 toString() should never fail
                this.checkConnection();
            }
            // return realConnection.method(args);
            return method.invoke(realConnection, args);
        } catch (Throwable t) {
            throw ReflectionExceptionUnwrappedMaker.unwrapThrowable(t);
        }
    }

}
