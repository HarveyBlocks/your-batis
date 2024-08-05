package org.harvey.batis.datasource;

import lombok.Data;
import org.harvey.batis.enums.TransactionIsolationLevel;
import org.harvey.batis.io.Resources;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * 没有被连接池池化的DataSource
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-24 09:48
 */
@Data
public class UnpooledDataSource implements DataSource {
    private ClassLoader driverClassLoader;
    private Properties driverProperties;
    /**
     * 享元模式, 不重复创建Driver
     */
    private static Map<String, Driver> registeredDrivers = new ConcurrentHashMap<>();

    /**
     * Driver类类名, 实现{@link java.sql.Driver}接口的类, <br>
     * 可以是数据库的JDBC提供如{@link com.mysql.jdbc.Driver},<br>
     * 也可以是数据库连接池提供
     */
    private String driverClassName;

    private String url;
    private String username;
    private String password;

    /**
     * 是否开启自动提交事务
     */
    private Boolean autoCommit;
    /**
     * 默认事务隔离级别
     * 想用枚举{@link TransactionIsolationLevel}的, 但是由于魔数会警告, 故放弃
     */
    private Integer defaultTransactionIsolationLevel;
    /**
     * 设置默认网络超时值, 用于等待数据库操作完成.
     */
    private Integer defaultNetworkTimeout;

    static {
        // 本类在加载时, 就将程序中已经存在的drivers存入本类的静态字段
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            registeredDrivers.put(driver.getClass().getName(), driver);
        }
    }

    public UnpooledDataSource() {
    }

    private UnpooledDataSource(String driverClassName, String url) {
        this.driverClassName = driverClassName;
        this.url = url;
    }

    public UnpooledDataSource(String driverClassName, String url, String username, String password) {
        this(driverClassName, url);
        this.username = username;
        this.password = password;
    }

    public UnpooledDataSource(String driverClassName, String url, Properties driverProperties) {
        this(driverClassName, url);
        this.driverProperties = driverProperties;
    }


    public UnpooledDataSource(ClassLoader driverClassLoader, String driverClassName, String url, String username, String password) {
        this(driverClassName, url, username, password);
        this.driverClassLoader = driverClassLoader;
    }

    public UnpooledDataSource(ClassLoader driverClassLoader, String driverClassName, String url, Properties driverProperties) {
        this(driverClassName, url, driverProperties);
        this.driverClassLoader = driverClassLoader;
    }

    /**
     * @see #doGetConnection(String, String)
     */
    @Override
    public Connection getConnection() throws SQLException {
        return this.doGetConnection(username, password);
    }

    /**
     * @see #doGetConnection(String, String)
     */
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return this.doGetConnection(username, password);
    }

    /**
     * 依据username和password和当前字段的{@link #driverProperties}组建properties
     *
     * @see #doGetConnection(Properties)
     */
    private Connection doGetConnection(String username, String password) throws SQLException {
        Properties props = new Properties();
        if (driverProperties != null) {
            props.putAll(driverProperties);
        }
        if (username != null) {
            props.setProperty("user", username);
        }
        if (password != null) {
            props.setProperty("password", password);
        }
        return this.doGetConnection(props);
    }

    /**
     * 从{@link DriverManager}, 依据字段中的url和properties的auth<br>
     * 创建<b>新</b>Connect之后, 将当前对象的部分字段({@link #configureConnection(Connection)})注入Connect<br>
     *
     * @param info 含有名为"user"的key表示username, 和名为"password"的key, 其他自定
     * @return 新的连接
     */
    private Connection doGetConnection(Properties info) throws SQLException {
        this.initializeDriver();
        Connection connection = DriverManager.getConnection(url, info);
        this.configureConnection(connection);
        return connection;
    }


    /**
     * 创建Driver实例, 包装后注册到到{@link #registeredDrivers}和{@link DriverManager}<br>
     * 涉及字段registeredDrivers, 是静态字段, 不同对象都可以访问, 故上锁实现线程安全
     */
    private synchronized void initializeDriver() throws SQLException {
        if (registeredDrivers.containsKey(driverClassName)) {
            // 已经初始化过
            return;
        }
        // 还没初始化过
        try {
            Class<?> driverType;
            if (driverClassLoader != null) {
                // 直接用类加载器加载类
                driverType = Class.forName(driverClassName, true, driverClassLoader);
            } else {
                // 使用Resources工具加载类, Resources自带一些类加载器
                driverType = Resources.classForName(driverClassName);
            }
            // DriverManager 要求通过系统 ClassLoader 加载驱动程序
            Driver driverInstance = (Driver) driverType.getDeclaredConstructor().newInstance();
            // 将Driver包装后注册到DriverManager
            DriverManager.registerDriver(new DriverProxy(driverInstance));
            // 同时注册到本类的字段registeredDrivers
            registeredDrivers.put(driverClassName, driverInstance);
        } catch (Exception e) {
            throw new SQLException("Error setting driver on UnpooledDataSource. Cause: " + e);
        }
    }

    /**
     * 将当前对象的字段中的: <br>
     * {@link #defaultNetworkTimeout}, <br>
     * {@link #defaultTransactionIsolationLevel},<br>
     * {@link #autoCommit}注入Connect<br>
     */
    private void configureConnection(Connection conn) throws SQLException {
        if (defaultNetworkTimeout != null) {
            conn.setNetworkTimeout(Executors.newSingleThreadExecutor(), defaultNetworkTimeout);
        }
        if (autoCommit != null && autoCommit != conn.getAutoCommit()) {
            conn.setAutoCommit(autoCommit);
        }
        if (defaultTransactionIsolationLevel != null) {
            conn.setTransactionIsolation(defaultTransactionIsolationLevel);
        }
    }

    // Getter/Setter

    @Override
    public void setLoginTimeout(int loginTimeout) {
        DriverManager.setLoginTimeout(loginTimeout);
    }

    @Override
    public int getLoginTimeout() {
        return DriverManager.getLoginTimeout();
    }

    @Override
    public void setLogWriter(PrintWriter logWriter) {
        DriverManager.setLogWriter(logWriter);
    }

    @Override
    public PrintWriter getLogWriter() {
        return DriverManager.getLogWriter();
    }

    // -------

    @Override
    public <T> T unwrap(Class<T> interfaceClass) throws SQLException {
        throw new SQLException(getClass().getName() + " is not a wrapper.");
    }

    @Override
    public boolean isWrapperFor(Class<?> interfaceClass) {
        return false;
    }

    @Override
    public Logger getParentLogger() {
        // requires JDK version 1.6
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }
}
