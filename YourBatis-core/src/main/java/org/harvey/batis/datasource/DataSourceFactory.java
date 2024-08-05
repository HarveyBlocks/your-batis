package org.harvey.batis.datasource;


import javax.sql.DataSource;
import java.util.Properties;

/**
 * 用于线程池的连接等
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-24 09:30
 */
public interface DataSourceFactory {
    void setProperties(Properties properties);

    DataSource getDataSource();
}

