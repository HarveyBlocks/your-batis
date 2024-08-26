package org.harvey.batis.binding;

import lombok.Getter;
import org.harvey.batis.exception.UnfinishedFunctionException;
import org.harvey.batis.session.SqlSession;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mapper的代理工厂, 连接了Mapper接口和Mapper代理实现
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-02 13:35
 */
public class MapperProxyFactory<T> {

    @Getter
    private final Class<T> mapperInterface;
    @Getter
    private final Map<Method, MapperProxy.MapperMethodInvoker> methodCache = new ConcurrentHashMap<>();

    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    /**
     * @param mapperProxy 给出以何种方式实现{@link #mapperInterface}里的方法
     * @see Proxy#newProxyInstance(ClassLoader, Class[], InvocationHandler)
     */
    protected T newInstance(MapperProxy<T> mapperProxy) {
        ClassLoader classLoader = mapperInterface.getClassLoader();
        return (T) Proxy.newProxyInstance(classLoader, new Class[]{mapperInterface}, mapperProxy);
    }

    /**
     * 创建新的{@link MapperProxy}包装SqlSession,mapperInterface,methodCache, 实现mapperInterface
     *
     * @see #newInstance(MapperProxy)
     */
    public T newInstance(SqlSession sqlSession) {
        final MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface, methodCache);
        return this.newInstance(mapperProxy);
    }

}
