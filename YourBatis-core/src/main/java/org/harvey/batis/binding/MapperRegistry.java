package org.harvey.batis.binding;

import org.harvey.batis.config.Configuration;
import org.harvey.batis.exception.binding.BindingException;
import org.harvey.batis.session.SqlSession;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-02 13:31
 */
public class MapperRegistry {
    private final Configuration config;

    /**
     * TODO
     */
    private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();

    public MapperRegistry(Configuration config) {
        this.config = config;
    }


    /**
     * 依据type取出代理类的工厂, 然后实例化代理类
     *
     * @throws BindingException 不存在{@link #knownMappers}中就抛出异常; 实例化失败就抛出异常
     * @see MapperProxyFactory#newInstance(SqlSession)
     */
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
        if (mapperProxyFactory == null) {
            // 不存在就抛出异常
            throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
        }
        try {
            // 创建实例
            return mapperProxyFactory.newInstance(sqlSession);
        } catch (Exception e) {
            throw new BindingException("Error getting mapper instance. Cause: " + e, e);
        }
    }
}
