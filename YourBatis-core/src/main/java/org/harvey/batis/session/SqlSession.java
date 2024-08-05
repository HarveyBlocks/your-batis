package org.harvey.batis.session;

import org.harvey.batis.config.Configuration;

import java.io.Closeable;

/**
 * TODO
 * 会话
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-02 12:32
 */
public interface SqlSession extends Closeable {
    /**
     * TODO
     * 关闭当前会话
     */
    @Override
    void close();

    /**
     * TODO
     * 获取一个Mapper
     *
     * @param <T>  Mapper的类型
     * @param type Mapper的接口类型
     * @return 绑定到此 SqlSession 的 Mapper
     */
    <T> T getMapper(Class<T> type);


    Configuration getConfiguration();
}
