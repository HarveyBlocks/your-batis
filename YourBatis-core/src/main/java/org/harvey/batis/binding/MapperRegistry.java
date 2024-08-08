package org.harvey.batis.binding;

import org.harvey.batis.config.Configuration;
import org.harvey.batis.exception.UnfinishedFunctionException;
import org.harvey.batis.exception.binding.BindingException;
import org.harvey.batis.io.xml.ResolverUtil;
import org.harvey.batis.session.SqlSession;

import java.util.*;

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
     * Mapper接口类-Mapper代理工厂
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

    /**
     * @see #addMappers(String, Class)
     */
    public void addMappers(String packageName) {
        this.addMappers(packageName, Object.class);
    }

    /**
     * TODO
     *
     * @param packageName TODO 包名? XML文件? java package?
     * @param superType   父类类型
     * @see #addMapper(Class)
     */
    public void addMappers(String packageName, Class<?> superType) {
        ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<>();
        resolverUtil.find(new ResolverUtil.IsSonMatcher(superType), packageName);
        Set<Class<? extends Class<?>>> mapperSet = resolverUtil.getMatches();
        for (Class<?> mapperClass : mapperSet) {
            this.addMapper(mapperClass);
        }
    }


    /**
     * TODO
     * 联系Mapper的XML文件和Mapper接口, 组建Mapper代理
     */
    public <T> void addMapper(Class<T> type) {
        if (!type.isInterface()) {
            return;
        }
        if (this.hasMapper(type)) {
            throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
        }
        boolean loadCompleted = false;
        try {
            knownMappers.put(type, new MapperProxyFactory<>(type));
            // It's important that the type is added before the parser is run
            // otherwise the binding may automatically be attempted by the
            // mapper parser. If the type is already known, it won't try.
            throw new UnfinishedFunctionException();
        } finally {
            if (!loadCompleted) {
                knownMappers.remove(type);
            }
        }
    }

    public <T> boolean hasMapper(Class<T> type) {
        return knownMappers.containsKey(type);
    }

    /**
     * @return 所有Mapper的代理
     */
    public Collection<Class<?>> getMappers() {
        return Collections.unmodifiableCollection(knownMappers.keySet());
    }
}
