package org.harvey.batis.datasource;

import lombok.Getter;
import lombok.Setter;
import org.harvey.batis.io.log.Log;
import org.harvey.batis.io.log.LogFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;


/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-01 12:47
 */
public class PooledDataSource implements DataSource {
    private static final Log LOG = LogFactory.getLog(PooledDataSource.class);

    /**
     * 不使用继承, 而是依赖
     */
    private final UnpooledDataSource dataSource;

    /**
     * 预期的连接类型代码
     * 由url, username, password共同决定
     * 🤔 : 有没有必要专门搞一个ConnectType出来?
     */
    private PooledConnection.ConnectionType expectedConnectionTypeCode;

    @Getter
    private final PoolState poolState = new PoolState(this);

    // 可选配置字段
    /**
     * 活动连接的最大数
     */
    @Getter
    protected int poolMaximumActiveConnections = 10;
    /**
     * 空闲连接的最大数
     */
    @Getter
    protected int poolMaximumIdleConnections = 5;
    /**
     * 连接在可能再次被放弃之前可以使用的最长时间。
     */
    @Getter
    protected int poolMaximumCheckoutTime = 20000;
    /**
     * 在重试获取连接之前等待的时间
     */
    @Getter
    protected int poolTimeToWait = 20000;
    /**
     * 一个线程中错误连接发生的最大容忍次数<br>
     * 该线程在超出后会申请新的 {@link PooledConnection}.
     */
    @Getter
    @Setter
    protected int poolMaximumLocalBadConnectionTolerance = 3;
    /**
     * 用于检查连接的查询语句, 默认是一个错误的语句
     */
    @Getter
    protected String poolPingQuery = "YOU SHOULD SET A QUERY SQL TO PING YOUR CONNECTION;";
    /**
     * 确定是否应使用 ping 查询。
     */
    @Getter
    protected boolean poolPingEnabled;
    /**
     * 如果在这么多毫秒内未使用连接，请 ping 数据库以确保连接仍然良好。
     */
    @Getter
    protected int poolPingConnectionsNotUsedFor;


    public PooledDataSource(UnpooledDataSource dataSource) {
        this.dataSource = dataSource;
        this.expectedConnectionTypeCode = new PooledConnection.ConnectionType(dataSource);
    }

    public PooledDataSource() {
        this(new UnpooledDataSource());
    }

    public PooledDataSource(String driver, String url, String username, String password) {
        this(new UnpooledDataSource(driver, url, username, password));
    }


    public PooledDataSource(String driver, String url, Properties driverProperties) {
        this(new UnpooledDataSource(driver, url, driverProperties));
    }

    public PooledDataSource(ClassLoader driverClassLoader, String driver, String url, String username, String password) {
        this(new UnpooledDataSource(driverClassLoader, driver, url, username, password));
    }

    public PooledDataSource(ClassLoader driverClassLoader, String driver, String url, Properties driverProperties) {
        this(new UnpooledDataSource(driverClassLoader, driver, url, driverProperties));
    }



    /**
     * @see #poolMaximumActiveConnections
     */
    public void setPoolMaximumActiveConnections(int poolMaximumActiveConnections) {
        this.poolMaximumActiveConnections = poolMaximumActiveConnections;
        this.forceCloseAll();
    }

    /**
     * @see #poolMaximumIdleConnections
     */
    public void setPoolMaximumIdleConnections(int poolMaximumIdleConnections) {
        this.poolMaximumIdleConnections = poolMaximumIdleConnections;
        this.forceCloseAll();
    }

    /**
     * @param poolMaximumCheckoutTime The maximum time
     * @see #poolMaximumCheckoutTime
     */
    public void setPoolMaximumCheckoutTime(int poolMaximumCheckoutTime) {
        this.poolMaximumCheckoutTime = poolMaximumCheckoutTime;
        this.forceCloseAll();
    }

    /**
     * @see #poolTimeToWait
     */
    public void setPoolTimeToWait(int poolTimeToWait) {
        this.poolTimeToWait = poolTimeToWait;
        this.forceCloseAll();
    }

    /**
     * @see #poolPingQuery
     */
    public void setPoolPingQuery(String poolPingQuery) {
        this.poolPingQuery = poolPingQuery;
        this.forceCloseAll();
    }

    /**
     * @param poolPingEnabled True 开启 ping 查询
     * @see #poolPingEnabled
     */
    public void setPoolPingEnabled(boolean poolPingEnabled) {
        this.poolPingEnabled = poolPingEnabled;
        this.forceCloseAll();
    }

    /**
     * @see #poolPingConnectionsNotUsedFor
     */
    public void setPoolPingConnectionsNotUsedFor(int milliseconds) {
        this.poolPingConnectionsNotUsedFor = milliseconds;
        this.forceCloseAll();
    }

    /**
     * @see UnpooledDataSource#setDriverClassName(String)
     */
    public void setDriverClassName(String driverClassName) {
        dataSource.setDriverClassName(driverClassName);
        this.forceCloseAll();
    }

    /**
     * @see UnpooledDataSource#setUrl(String)
     */
    public void setUrl(String url) {
        dataSource.setUrl(url);
        this.forceCloseAll();
    }

    /**
     * @see UnpooledDataSource#setUsername(String)
     */
    public void setUsername(String username) {
        dataSource.setUsername(username);
        this.forceCloseAll();
    }

    /**
     * @see UnpooledDataSource#setPassword(String)
     */
    public void setPassword(String password) {
        dataSource.setPassword(password);
        this.forceCloseAll();
    }

    /**
     * @see UnpooledDataSource#setAutoCommit(Boolean)
     */
    public void setDefaultAutoCommit(boolean defaultAutoCommit) {
        dataSource.setAutoCommit(defaultAutoCommit);
        this.forceCloseAll();
    }

    /**
     * @see UnpooledDataSource#setDefaultTransactionIsolationLevel(Integer)
     */
    public void setDefaultTransactionIsolationLevel(Integer defaultTransactionIsolationLevel) {
        dataSource.setDefaultTransactionIsolationLevel(defaultTransactionIsolationLevel);
        this.forceCloseAll();
    }

    /**
     * @see UnpooledDataSource#setDriverProperties(Properties)
     */
    public void setDriverProperties(Properties driverProps) {
        dataSource.setDriverProperties(driverProps);
        this.forceCloseAll();
    }

    /**
     * 参考 {@link Connection#setNetworkTimeout(java.util.concurrent.Executor, int)}
     *
     * @see UnpooledDataSource#setDefaultNetworkTimeout(Integer)
     */
    public void setDefaultNetworkTimeout(Integer milliseconds) {
        dataSource.setDefaultNetworkTimeout(milliseconds);
        this.forceCloseAll();
    }

    /**
     * @see UnpooledDataSource#getDriverClassName()
     */
    public String getDriverClassName() {
        return dataSource.getDriverClassName();
    }

    /**
     * @see UnpooledDataSource#getUrl()
     */
    public String getUrl() {
        return dataSource.getUrl();
    }

    /**
     * @see UnpooledDataSource#getUsername()
     */
    public String getUsername() {
        return dataSource.getUsername();
    }

    /**
     * @see UnpooledDataSource#getPassword()
     */
    public String getPassword() {
        return dataSource.getPassword();
    }

    /**
     * @see UnpooledDataSource#getAutoCommit()
     */
    public boolean isAutoCommit() {
        return Boolean.TRUE.equals(dataSource.getAutoCommit());
    }

    /**
     * @see UnpooledDataSource#getDefaultTransactionIsolationLevel()
     */
    public Integer getDefaultTransactionIsolationLevel() {
        return dataSource.getDefaultTransactionIsolationLevel();
    }

    /**
     * @see UnpooledDataSource#getDriverProperties()
     */
    public Properties getDriverProperties() {
        return dataSource.getDriverProperties();
    }

    /**
     * @see UnpooledDataSource#getDefaultNetworkTimeout()
     */
    public Integer getDefaultNetworkTimeout() {
        return dataSource.getDefaultNetworkTimeout();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return this.popConnection(dataSource.getUsername(), dataSource.getPassword()).getProxyConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return this.popConnection(username, password).getProxyConnection();
    }

    /**
     * @see #popConnection(String, String, int) 和其一起构成递归
     * @deprecated 这个方法不再维护poolState中繁杂的字段, 用于帮助思考
     */
    private PooledConnection popConnectionFixInterruptedException(String username, String password, int localBadConnectionCount)
            throws SQLException {
        try {
            return this.popConnection(username, password, localBadConnectionCount);
        } catch (InterruptedException e) {
            String msg = "PooledDataSource: Unknown severe error condition.  " +
                    "The connection pool returned a null connection.";
            LOG.debugIfEnable(msg);
            throw new SQLException(msg);
        }
    }

    /**
     * @param localBadConnectionCount 每条线程各自的连接测试失败数量
     * @see #popConnection(String, String)
     * @deprecated 这个方法不再维护poolState中繁杂的字段, 用于帮助思考
     */
    private PooledConnection popConnection(String username, String password, int localBadConnectionCount)
            throws SQLException, InterruptedException {
        PooledConnection resultConn; // 最终需要返回的结果

        synchronized (poolState) {
            // 同一个池状态的不能进, 要等钥匙
            if (!poolState.idleConnections.isEmpty()) {
                // 池有可用的空闲连接
                // 拿出一个连接
                resultConn = poolState.idleConnections.remove(0);
                // 已从池中检出连接+HashCode
                LOG.debugIfEnable("Checked out connection " + resultConn.getRealHashCode() + " from pool.");
            } else {
                // 池没有可用的空闲连接
                if (poolState.getActiveConnectionCount() < poolMaximumActiveConnections) {
                    // 活跃的连接数量没有到达上限, 还可以创建新活跃的连接
                    resultConn = new PooledConnection(dataSource.getConnection(), this);
                    LOG.debugIfEnable("Created connection " + resultConn.getRealHashCode() + ".");
                } else {
                    // 不能创建连接
                    // 拿出一个最老的连接
                    PooledConnection oldestActiveConnection = poolState.activeConnections.get(0);
                    // 获取这个最老连接上次检查该连接过去的时间间隔
                    long longestCheckoutTime = oldestActiveConnection.getCheckoutTime();
                    if (longestCheckoutTime > poolMaximumCheckoutTime) {
                        // 这个时间间隔太长了, 是逾期检查的连接
                        resultConn = this.claimedOverdueConnect(oldestActiveConnection);
                    } else {
                        // 这个连接还没有逾期
                        // 等待....
                        this.waitConnectFinishWork();
                        // 再次递归, 尝试
                        return popConnectionFixInterruptedException(username, password, localBadConnectionCount);
                    }
                }
            }
            // 该线程已经获取了它的resultConn
            // ping 服务器以检查连接是否有效
            if (resultConn.isValid()) {
                // 老连接可用且ping通了
                // 更新连接字段
                this.addConnection2PoolStateActiveList(resultConn, username, password);
            } else {
                // 老连接不能ping通
                // 失败的情况
                localBadConnectionCount++;
                String debugMessage = "A bad connection (" +
                        resultConn.getRealHashCode() +
                        ") was returned from the pool, getting another connection.";
                LOG.debugIfEnable(debugMessage);
                resultConn = null;
                this.checkHasConnectionToTry(localBadConnectionCount);
            }
        }

        if (resultConn != null) {
            // 递归出口
            return resultConn;
        } else {
            return popConnectionFixInterruptedException(username, password, localBadConnectionCount);
        }
    }

    /**
     * 从{@link PoolState#idleConnections}中删去空闲Connection, <br>
     * 或创建新的Connection,<br>
     * 从{@link PoolState#activeConnections}头拿出oldConnect(不删除):
     * <li>oldConnect过期(超时任务), 则中断, 该超时任务, 该oldConnect将用于新任务 </li>
     * <li>oldConnect未过期, 等待, 再次尝试获取Connection</li>
     * <p></p>
     * 测试连接是否可用
     * <li> (如果可用则)放入{@link PoolState#activeConnections}尾, 并返回, 并返回 </li>
     * <li> 连接不可用, 再次尝试获取Connection, <br>
     * 若尝试了所有的Connection还不行就抛出异常 </li>
     *
     * @throws SQLException 可能是因为等待时发生的线程异常,<br>
     *                      可能是因为可以用的连接一个都没有,<br>
     *                      等......
     */
    private PooledConnection popConnection(String username, String password) throws SQLException {
        boolean countedWait = false; // 是否已经将等待这一事件加到统计这一事件的字段中去了
        PooledConnection resultConn = null; // 最终需要返回的结果
        long requestStartTimestamp = System.currentTimeMillis(); // 本次请求开始的时间
        int localBadConnectionCount = 0; // 每条线程各自的连接测试失败数量

        while (resultConn == null) {
            synchronized (poolState) {
                // 同一个池状态的不能进, 要等钥匙

                //          池有没有空闲的连接?
                //          /              \
                //         有               没有
                //         /                  \
                //   从空闲中取出一个,            还能创建活跃的连接吗?
                //  赋值给resultConn           /                \
                //                          不能                能
                //                          /                   \
                //              拿出最老的那个活跃连接         创建一个未池化的连接,
                //                        |              用PooledConnection包装,
                //                        ↓                 赋值给resultConn
                //              这个最老连接的检查时间过了吗?
                //                /               \
                //              没过              过了
                //              /                   \
                //          等待它一段时间         此时超时, 中断之前的任务
                //           然后继续循环         给老链接换PooledConnection新包装,
                //    等待过程中线程异常就跳出循环     然后将新报装赋值给resultConn,
                //                                  把老连接的老包装失效

                // 可以看出, 只有在没过检查时间的情况下是没有resultConn的
                if (!poolState.idleConnections.isEmpty()) {
                    // 池有可用的空闲连接
                    // 拿出一个连接
                    resultConn = poolState.idleConnections.remove(0);
                    // 已从池中检出连接+HashCode
                    LOG.debugIfEnable("Checked out connection " + resultConn.getRealHashCode() + " from pool.");
                } else {
                    // 池没有可用的空闲连接
                    if (poolState.getActiveConnectionCount() < poolMaximumActiveConnections) {
                        // 活跃的连接数量没有到达上限, 还可以创建新活跃的连接
                        resultConn = new PooledConnection(dataSource.getConnection(), this);
                        LOG.debugIfEnable("Created connection " + resultConn.getRealHashCode() + ".");
                    } else {
                        // 不能创建连接
                        // 拿出一个最老的连接
                        PooledConnection oldestActiveConnection = poolState.activeConnections.get(0);
                        // 获取这个最老连接上次检查该连接过去的时间间隔
                        long longestCheckoutTime = oldestActiveConnection.getCheckoutTime();
                        if (longestCheckoutTime > poolMaximumCheckoutTime) {
                            // 这个时间间隔太长了, 是逾期检查的连接
                            resultConn = this.claimedOverdueConnect(longestCheckoutTime, oldestActiveConnection);
                        } else {
                            // 这个连接还没有逾期
                            // 等待....
                            try {
                                this.waitConnectFinishWork(countedWait);
                                // 一个线程, 凡是进入过该函数, 无论第几次, 都已经记录过Wait了, 所以必返回true
                                countedWait = true;
                            } catch (InterruptedException e) {
                                // 出现线程异常就跳出循环
                                break;
                            }
                            continue;
                        }

                    }
                }
                // 该线程已经获取了它的resultConn

                //         用resultConn ping 服务器以检查连接是否有效
                //              /               \
                //            有效               无效
                //            /                     \
                //     将resultConn保存         无效次数是否到达上限
                // 由于多线程, 该连接可能又做新任务    /          \
                //       中断之前的任务           达到        未达到
                //                             /             \
                //                          抛出异常       将resultConn重新置为null,
                //                                          继续循环尝试


                if (resultConn.isValid()) {
                    // 老连接可用且ping通了
                    // 更新连接字段
                    this.addConnection2PoolStateActiveList(resultConn, username, password);
                    // 这一次请求(找到)的计时记录
                    // 🤔 : 这哪里来的一次请求? 什么叫"一次请求"
                    poolState.requestCount++;
                    poolState.accumulatedRequestTime += System.currentTimeMillis() - requestStartTimestamp;
                } else {
                    // 老连接不能ping通
                    // 失败的情况
                    localBadConnectionCount++;
                    poolState.badConnectionCount++;
                    String debugMessage = "A bad connection (" +
                            resultConn.getRealHashCode() +
                            ") was returned from the pool, getting another connection.";
                    LOG.debugIfEnable(debugMessage);
                    resultConn = null;
                    // 如果失败就抛出异常
                    this.checkHasConnectionToTry(localBadConnectionCount);
                }
            }

        }
        // 要跳出循环, 要么resultConn不为null, 要么break, 要么抛出异常
        // 如果要到此处, 一定是break
        if (resultConn == null) {
            // 一定出现线程异常就跳出循环的情况?
            // 未知的严重错误情况。连接池返回了 null 连接。
            String msg = "PooledDataSource: Unknown severe error condition.  " +
                    "The connection pool returned a null connection.";
            LOG.debugIfEnable(msg);
            throw new SQLException(msg);
        }

        return resultConn;
    }

    /**
     * @param localBadConnectionCount 尝试连接失败个数
     * @throws SQLException 当连接失败个数超过所有可创建连接({@link #poolMaximumActiveConnections}+
     *                      {@link #poolMaximumIdleConnections})之后, 还是失败, 没有可以试的连接后抛出异常
     */
    private void checkHasConnectionToTry(int localBadConnectionCount) throws SQLException {
        if (localBadConnectionCount > (poolMaximumIdleConnections + poolMaximumLocalBadConnectionTolerance)) {
            // 即使在增加了连接数, 直到连接上限之后, 还是所有连接都不可用
            String exceptionMsg = "PooledDataSource: Could not get a good connection to the database.";
            LOG.debugIfEnable(exceptionMsg);
            throw new SQLException(exceptionMsg);
        }
    }

    /**
     * 维护{@link #poolState}中的字段:<br>
     * {@link PoolState#claimedOverdueConnectionCount}和<br>
     * {@link PoolState#accumulatedCheckoutTimeOfOverdueConnections}和<br>
     * {@link PoolState#accumulatedCheckoutTime}<br>
     * 预期次数加一, 逾期时间累计, 检查时间累计
     *
     * @see #claimedOverdueConnect(PooledConnection)
     */
    private PooledConnection claimedOverdueConnect(
            long longestCheckoutTime,
            PooledConnection oldestActiveConnection) throws SQLException {
        // 可以申请逾期连接
        poolState.claimedOverdueConnectionCount++;
        // 累计逾期检查消耗的时间
        poolState.accumulatedCheckoutTimeOfOverdueConnections += longestCheckoutTime;
        // 累计检查消耗的时间
        poolState.accumulatedCheckoutTime += longestCheckoutTime;
        return this.claimedOverdueConnect(oldestActiveConnection);
    }

    /**
     * 从{@link PoolState#activeConnections}中删除param oldestActiveConnection之后<br>
     * 对oldestActiveConnection进行重新包装{@link PooledConnection}
     *
     * @param oldestActiveConnection 老连接
     */
    private PooledConnection claimedOverdueConnect(PooledConnection oldestActiveConnection) throws SQLException {
        // 去除该逾期连接
        poolState.activeConnections.remove(oldestActiveConnection);
        Connection realConnection = oldestActiveConnection.getRealConnection();
        // 超时了, 无论该连接现在在做什么操作, 都要放弃
        if (!realConnection.getAutoCommit()) {
            // 该连接不能自动提交
            try {
                // 🤔 : 为何回滚? 针对是为了放弃之前的操作吗?
                realConnection.rollback();
            } catch (SQLException e) {
                // 只需记录一条消息进行调试，然后继续执行以下语句
                // 就像什么都没发生一样。
                // 用新的 PooledConnection 包装不良连接
                // 为了不中断当前正在执行的线程，并使当前线程有机会加入下一个竞争，以获得另一个有效的数据库连接。

                // 🤔 : 为啥这里就不见检查是否可用DEBUG了?
                // LOG.debug("Bad connection. Could not roll back");
                LOG.debugIfEnable("Bad connection. Could not roll back");
            }
        }
        // 用新的 PooledConnection 包装不良连接
        PooledConnection resultConn = new PooledConnection(realConnection, this);
        // 但是创建的时间需要是老连接的, 因为不是新创建的连接, 而是新创建的包装
        // 已在PooledConnection构造器中完成
        // resultConn.setCreatedTimestamp(oldestActiveConnection.getCreatedTimestamp());
        // resultConn.setLastUsedTimestamp(oldestActiveConnection.getLastUsedTimestamp());

        // 老连接的老包装无效化
        // 老链接的新包装依旧有效
        oldestActiveConnection.invalidate();
        // 声明一个逾期连接
        LOG.debugIfEnable("Claimed overdue connection " + resultConn.getRealHashCode() + ".");
        return resultConn;
    }

    /**
     * 更新该连接的配置之后, 加入{@link #poolState}的字段{@link PoolState#activeConnections}中
     *
     * @param resultConn 需要被加入{@link PoolState#activeConnections}中的连接, 要完成新任务
     */
    private void addConnection2PoolStateActiveList(PooledConnection resultConn, String username, String password)
            throws SQLException {
        // 🤔 : 结束还没有完成的工作? 真的吗?
        PooledDataSource.notAutoCommitThenRollback(resultConn.getRealConnection());
        // 更新该连接的配置之后
        resultConn.setConnectionTypeCode(new PooledConnection.ConnectionType(dataSource.getUrl(), username, password));
        resultConn.setCheckoutTimestamp(System.currentTimeMillis());
        resultConn.setLastUsedTimestamp(System.currentTimeMillis());
        // 存入List
        poolState.activeConnections.add(resultConn);
    }

    /**
     * 将当前线程等待, 将等待这一事件记录到统计字段中, 并累加等待时间<br>
     * 维护{@link #poolState}中的字段{@link PoolState#hadToWaitCount}和{@link PoolState#accumulatedWaitTime}
     *
     * @see #waitConnectFinishWork()
     */
    private void waitConnectFinishWork(boolean countedWait) throws InterruptedException {
        if (!countedWait) {
            // 如果曾经等待过, 一个线程, 一个循环中, 多次等待只认为是一次等待
            // 增加必须等待次数
            poolState.hadToWaitCount++;
        }
        long startWaitTimestamp = System.currentTimeMillis();
        this.waitConnectFinishWork();
        // 累计等待时间增加
        poolState.accumulatedWaitTime += System.currentTimeMillis() - startWaitTimestamp;
    }

    /**
     * 等待时长为{@link #poolTimeToWait}
     *
     * @throws InterruptedException 线程异常
     */
    private void waitConnectFinishWork() throws InterruptedException {
        LOG.debugIfEnable("Waiting as long as " + poolTimeToWait + " milliseconds for connection.");
        // 当前线程阻塞, 其他线程争抢这把锁(如果要抢的是这把锁的话)
        poolState.wait(poolTimeToWait);
    }

    private static void notAutoCommitThenRollback(Connection resultConn) throws SQLException {
        if (!resultConn.getAutoCommit()) {
            resultConn.rollback();
        }
    }

    /**
     * @see DriverManager#setLoginTimeout(int)
     */
    @Override
    public void setLoginTimeout(int loginTimeout) {
        DriverManager.setLoginTimeout(loginTimeout);
    }

    /**
     * @see DriverManager#getLoginTimeout()
     */
    @Override
    public int getLoginTimeout() {
        return DriverManager.getLoginTimeout();
    }

    /**
     * @see DriverManager#setLogWriter(PrintWriter)
     */
    @Override
    public void setLogWriter(PrintWriter logWriter) {
        DriverManager.setLogWriter(logWriter);
    }

    /**
     * @see DriverManager#getLogWriter()
     */
    @Override
    public PrintWriter getLogWriter() {
        return DriverManager.getLogWriter();
    }

    /**
     * @return {@code  Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);}
     */
    @Override
    public Logger getParentLogger() {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }

    /**
     * @param interfaceClass 是一个接口, 结果需要实现该接口
     * @throws SQLException 本类不是一个wrapper
     */
    @Override
    public <T> T unwrap(Class<T> interfaceClass) throws SQLException {
        throw new SQLException(getClass().getName() + " is not a wrapper.");
    }

    /**
     * @return false
     */
    @Override
    public boolean isWrapperFor(Class<?> interfaceClass) {
        return false;
    }

    /**
     * 关闭池中的所有活动和空闲连接。<br>
     * 在修改本类的字段时, 需要关闭所有连接, 因为配置被改变了, 所有连接的配置也要改变, 就要先全部停下来
     */
    public void forceCloseAll() {
        // 有同一个PoolState的, 同一个池的, 不能进
        synchronized (poolState) {
            // 更新DataSource状态码
            expectedConnectionTypeCode = new PooledConnection.ConnectionType(dataSource);
            closePooledConnections(poolState.activeConnections);
            closePooledConnections(poolState.idleConnections);
        }
        LOG.debugIfEnable("PooledDataSource forcefully closed/removed all connections.");
    }

    /**
     * 遍历所有连接, 逐一关闭
     */
    private void closePooledConnections(List<PooledConnection> pooledConnections) {
        for (int i = pooledConnections.size(); i > 0; i--) {
            try {
                // 取出最后一个PooledConnection
                PooledConnection conn = pooledConnections.remove(i - 1);
                // 将连接无效
                conn.invalidate();
                // 获取Connection本体
                Connection realConn = conn.getRealConnection();
                // 该连接的事务不是自动提交
                // 就回滚一下
                // 🤔 : 为啥要回滚呢?是考虑到关闭该连接的时候, 另一条线程上的该连接刚好还没提交吗?
                //      不确定...
                PooledDataSource.notAutoCommitThenRollback(realConn);
                // 关闭连接
                realConn.close();
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * 检查连接是否可用
     *
     * @param conn - 待检验的PooledConnection
     * @return 如果连接仍然可用，则返回 True
     */
    protected boolean pingConnection(PooledConnection conn) {
        try {
            // 连接已关闭则连接不可用, 否则, 连接可用
            boolean closed = conn.getRealConnection().isClosed();
            if (closed) {
                return false;
            }
        } catch (SQLException e) {
            PooledDataSource.logConnectBad(conn, e);
            // 获取连接是否关闭失败, 认为连接不可用
            return false;
        }
        // 在检查连接未关闭后(result=true的情况)
        // 接下来ping这个连接来检查连接是否可用
        if (!poolPingEnabled) {
            // 不能ping进行进一步检查, 就返回true(不太严格的true)
            return true;
        }
        if (poolPingConnectionsNotUsedFor < 0) {
            // 类似于不能ping的情况
            return true;
        }
        if (poolPingConnectionsNotUsedFor >= conn.getTimeElapsedSinceLastUse()) {
            // 时机未到(才刚ping过, 不要ping得这么频繁)
            return true;
        }
        try {
            LOG.debugIfEnable("Testing connection " + conn.getRealHashCode() + " ...");
            Connection realConn = conn.getRealConnection();

            try (Statement stat = realConn.createStatement()) {
                stat.executeQuery(poolPingQuery) // 意思是说可能会注入, 教养不好了
                        .close(); // 关闭ResultSet
            }

            // 如果没有开启自动提交事务, 就回滚
            // 可能设置了这个语句是写的语句
            // (如果开启了自动提交就会在ping的时候不知不觉执行一些写操作)
            PooledDataSource.notAutoCommitThenRollback(realConn);

            // result = true;
            // 只有在result = true的时候才会进入该分支, 故不需要重新赋值
            LOG.debugIfEnable("Connection " + conn.getRealHashCode() + " is GOOD!");
        } catch (Exception e) {
            LOG.warn("Execution of ping query '" + poolPingQuery + "' failed: " + e.getMessage());
            // 当然如果poolPingQuery本身是错误的, 那么ping操作将一直显示不通过
            try {
                // ping失败了, 把这条连接关闭
                conn.getRealConnection().close();
            } catch (Exception ignore) {
                // ignore
                // 就算在关闭时有异常, 下一次再来的时候也不会认为这条连接是可用的了
            }
            PooledDataSource.logConnectBad(conn, e);
            return false;
        }
        // 活到最后的就是连接成功
        return true;
    }

    private static void logConnectBad(PooledConnection conn, Exception e) {
        LOG.debugIfEnable("Connection " + conn.getRealHashCode() + " is BAD: " + e.getMessage());
    }


    /**
     * 增加空闲的连接
     *
     * @param conn 检查该连接可用后, 存如{@link PoolState#idleConnections}
     */
    protected void pushConnection(PooledConnection conn) throws SQLException {
        synchronized (poolState) {
            poolState.activeConnections.remove(conn);
            if (!conn.isValid()) {
                // 无效的连接, 该连接试图加入到池, 放弃连接。
                LOG.debugIfEnable("A bad connection (" + conn.getRealHashCode() + ") attempted to return to the pool, " +
                        "discarding connection.");
                poolState.badConnectionCount++;
                return;
            }

            poolState.accumulatedCheckoutTime += conn.getCheckoutTime();
            PooledDataSource.notAutoCommitThenRollback(conn.getRealConnection());

            if (poolState.getIdleConnectionCount() >= poolMaximumIdleConnections // 空闲连接数已达上限, 不可以新增
                    || expectedConnectionTypeCode.equals(conn.getConnectionTypeCode())// 连接的目标和池的不目标一致
            ) {
                conn.getRealConnection().close();
                LOG.debugIfEnable("Closed connection " + conn.getRealHashCode() + ".");
                conn.invalidate();
                return;
            }
            // 连接可以加入池
            PooledConnection newConn = new PooledConnection(conn.getRealConnection(), this);
            poolState.idleConnections.add(newConn);

            // newConn.setCreatedTimestamp(conn.getCreatedTimestamp());
            // newConn.setLastUsedTimestamp(conn.getLastUsedTimestamp());
            // 已在PooledConnection构造器中完成

            // 使老的包装无效化
            conn.invalidate();
            LOG.debugIfEnable("Returned connection " + newConn.getRealHashCode() + " to pool.");
            poolState.notifyAll();
        }
    }
}
