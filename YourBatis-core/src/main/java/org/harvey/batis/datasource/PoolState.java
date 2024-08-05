package org.harvey.batis.datasource;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 本包中的PooledDataSource会将数据记录在该类的字段中
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-01 12:47
 */
public class PoolState {
    protected final PooledDataSource dataSource;

    /**
     * 空闲连接
     */
    protected final List<PooledConnection> idleConnections = new ArrayList<>();
    /**
     * 活动连接
     */
    protected final List<PooledConnection> activeConnections = new ArrayList<>();
    /**
     * 请求 计数
     */
    @Getter
    protected long requestCount = 0;
    /**
     * 累计请求 时间
     */
    protected long accumulatedRequestTime = 0;
    /**
     * 累计传输数据检查消耗 时间
     */
    protected long accumulatedCheckoutTime = 0;
    /**
     * 已声明的逾期连接 计数
     */
    @Getter
    protected long claimedOverdueConnectionCount = 0;
    /**
     * 逾期连接的累计传输数据检查消耗 时间
     */
    protected long accumulatedCheckoutTimeOfOverdueConnections = 0;
    /**
     * 累计等待时间
     */
    protected long accumulatedWaitTime = 0;
    /**
     * 不得不等待的情况 计数
     */
    @Getter
    protected long hadToWaitCount = 0;
    /**
     * 连接错误 计数
     */
    @Getter
    protected long badConnectionCount = 0;

    public PoolState(PooledDataSource dataSource) {
        this.dataSource = dataSource;
    }


    /**
     * @return 平均每次请求的时间
     */
    public synchronized long getAverageRequestTime() {
        return requestCount == 0 ? 0 : accumulatedRequestTime / requestCount;
    }

    /**
     * @return 平均每次等待的时间
     */
    public synchronized long getAverageWaitTime() {
        return hadToWaitCount == 0 ? 0 : accumulatedWaitTime / hadToWaitCount;
    }


    /**
     * @return 平均每次检查传输数据逾期的时间
     */
    public synchronized long getAverageOverdueCheckoutTime() {
        return claimedOverdueConnectionCount == 0 ? 0 : accumulatedCheckoutTimeOfOverdueConnections / claimedOverdueConnectionCount;
    }

    /**
     * @return 平均每次检查传输数据的时间
     */
    public synchronized long getAverageCheckoutTime() {
        return requestCount == 0 ? 0 : accumulatedCheckoutTime / requestCount;
    }

    /**
     * @return 空闲连接的数量
     */
    public synchronized int getIdleConnectionCount() {
        return idleConnections.size();
    }

    /**
     * @return 活动连接的数量
     */
    public synchronized int getActiveConnectionCount() {
        return activeConnections.size();
    }

    @Override
    public synchronized String toString() {
        return "\n====CONFIGURATION==============================================" +
                "\n jdbcDriver                     " + dataSource.getDriverClassName() +
                "\n jdbcUrl                        " + dataSource.getUrl() +
                "\n jdbcUsername                   " + dataSource.getUsername() +
                "\n jdbcPassword                   " + (dataSource.getPassword() == null ? "NULL" : "************") +
                "\n poolMaxActiveConnections       " + dataSource.poolMaximumActiveConnections +
                "\n poolMaxIdleConnections         " + dataSource.poolMaximumIdleConnections +
                "\n poolMaxCheckoutTime            " + dataSource.poolMaximumCheckoutTime +
                "\n poolTimeToWait                 " + dataSource.poolTimeToWait +
                "\n poolPingEnabled                " + dataSource.poolPingEnabled +
                "\n poolPingQuery                  " + dataSource.poolPingQuery +
                "\n poolPingConnectionsNotUsedFor  " + dataSource.poolPingConnectionsNotUsedFor +
                "\n ----STATUS-----------------------------------------------------" +
                "\n activeConnections              " + this.getActiveConnectionCount() +
                "\n idleConnections                " + this.getIdleConnectionCount() +
                "\n requestCount                   " + this.getRequestCount() +
                "\n averageRequestTime             " + this.getAverageRequestTime() +
                "\n averageCheckoutTime            " + this.getAverageCheckoutTime() +
                "\n claimedOverdue                 " + this.getClaimedOverdueConnectionCount() +
                "\n averageOverdueCheckoutTime     " + this.getAverageOverdueCheckoutTime() +
                "\n hadToWait                      " + this.getHadToWaitCount() +
                "\n averageWaitTime                " + this.getAverageWaitTime() +
                "\n badConnectionCount             " + this.getBadConnectionCount() +
                "\n================================================================";
    }
}
