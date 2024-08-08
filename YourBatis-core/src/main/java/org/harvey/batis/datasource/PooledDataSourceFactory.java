package org.harvey.batis.datasource;

import lombok.Getter;
import lombok.Setter;

/**
 * 池化的DataSource工厂, 其方法实现完全由其父类{@link UnpooledDataSourceFactory}提供
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-01 12:47
 */
public class PooledDataSourceFactory extends UnpooledDataSourceFactory {

    public PooledDataSourceFactory() {
        super(new PooledDataSource());
    }
}
