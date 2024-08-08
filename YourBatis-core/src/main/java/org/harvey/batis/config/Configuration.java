package org.harvey.batis.config;

import lombok.Getter;
import lombok.Setter;
import org.harvey.batis.binding.MapperRegistry;
import org.harvey.batis.builder.CacheRefResolver;
import org.harvey.batis.builder.MethodResolver;
import org.harvey.batis.builder.ResultMapResolver;
import org.harvey.batis.builder.xml.XMLStatementBuilder;
import org.harvey.batis.enums.ExecutorType;
import org.harvey.batis.exception.UnfinishedFunctionException;
import org.harvey.batis.exception.builder.IncompleteElementException;
import org.harvey.batis.mapping.Environment;
import org.harvey.batis.mapping.MappedStatement;
import org.harvey.batis.reflection.factory.DefaultObjectFactory;
import org.harvey.batis.reflection.factory.ObjectFactory;
import org.harvey.batis.session.SqlSession;
import org.harvey.batis.util.StrictMap;

import java.lang.reflect.Method;
import java.util.*;

/**
 * TODO
 * 管理中心, 许多类需要被本来管理, 这些类就作为本类的字段, 由本类实例化<br>
 * 这些被管理的类需要直到自己的管理中心是哪个对象, 就将本类作为构造器的参数
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-02 13:28
 */
public class Configuration {
    @Setter
    protected Environment environment;
    @Getter
    @Setter
    protected ExecutorType defaultExecutorType = ExecutorType.SIMPLE;

    // ...
    @Setter
    @Getter
    protected Properties variables = new Properties();
    @Getter
    protected ObjectFactory objectFactory = new DefaultObjectFactory();
    // ...

    protected final MapperRegistry mapperRegistry = new MapperRegistry(this);

    /**
     * TODO
     */
    protected final Map<String, MappedStatement> mappedStatements =
            new StrictMap<MappedStatement>("Mapped Statements collection")
                    .conflictMessageProducer((savedValue, targetValue) ->
                            ". please check " + savedValue.getResource() + " and " + targetValue.getResource());


    // 以下四个ResultMap,Statement,CacheRef,Method统称为Statement
    protected final Collection<XMLStatementBuilder> incompleteStatements = new LinkedList<>();
    protected final Collection<CacheRefResolver> incompleteCacheRefs = new LinkedList<>();
    protected final Collection<ResultMapResolver> incompleteResultMaps = new LinkedList<>();
    protected final Collection<MethodResolver> incompleteMethods = new LinkedList<>();


    public Configuration(Environment environment) {
        this();
        this.environment = environment;
    }

    /**
     * TODO
     */
    public Configuration() {
        new UnfinishedFunctionException().printStackTrace(System.err);
    }

    /**
     * 从{@link #mapperRegistry}中取出Mapper
     *
     * @see MapperRegistry#getMapper(Class, SqlSession)
     */
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        return mapperRegistry.getMapper(type, sqlSession);
    }

    /**
     * 转变成{@link #mappedStatements}的Key, 有个规范<br>
     * {@code type.getName() + "." + method.getName();}
     *
     * @see #mappedStatements
     */
    public static String statementKey(Class<?> type, Method method) {
        return type.getName() + "." + method.getName();
    }


    /**
     * @param statementName {@link #statementKey(Class, Method)}
     * @see #mappedStatements
     * @see java.util.HashMap#containsKey(Object)
     */
    public boolean hasStatement(String statementName) {
        return this.hasStatement(statementName, true);
    }

    /**
     * @param statementName                {@link #statementKey(Class, Method)}
     * @param validateIncompleteStatements 验证不完整的语句,{@link #buildAllStatements()}
     * @see #mappedStatements
     * @see java.util.HashMap#containsKey(Object)
     */
    public boolean hasStatement(String statementName, boolean validateIncompleteStatements) {
        if (validateIncompleteStatements) {
            // TODO this.buildAllStatements();
        }
        return mappedStatements.containsKey(statementName);
    }

    /**
     * @param id {@link #statementKey(Class, Method)}
     * @see #mappedStatements
     * @see #statementKey(Class, Method)
     * @see java.util.HashMap#get(Object)
     */
    public MappedStatement getMappedStatement(String id) {
        return this.getMappedStatement(id, true);
    }

    /**
     * @param id                           {@link #statementKey(Class, Method)}
     * @param validateIncompleteStatements 验证未完成的Statement,{@link #buildAllStatements()}
     * @see #mappedStatements
     * @see java.util.HashMap#get(Object)
     */
    public MappedStatement getMappedStatement(String id, boolean validateIncompleteStatements) {
        if (validateIncompleteStatements) {
            // TODO this.buildAllStatements();
        }
        return mappedStatements.get(id);
    }

    /**
     * TODO
     * Parses all the unprocessed statement nodes in the cache. It is recommended
     * to call this method once all the mappers are added as it provides fail-fast
     * statement validation.
     * 分析缓存中所有未处理的语句节点。建议在添加所有Mappers后调用此方法，因为它提供快速失败语句验证。
     */
    protected void buildAllStatements() {
        this.parsePendingResultMaps();
        if (!incompleteCacheRefs.isEmpty()) {
            synchronized (incompleteCacheRefs) {
                incompleteCacheRefs.removeIf(x -> x.resolveCacheRef() != null);
            }
        }
        if (!incompleteStatements.isEmpty()) {
            synchronized (incompleteStatements) {
                incompleteStatements.removeIf(x -> {
                    x.parseStatementNode();
                    return true;
                });
            }
        }
        if (!incompleteMethods.isEmpty()) {
            synchronized (incompleteMethods) {
                // 将未处理Method的全部处理掉, 然后返回从未处理集合中删除该Method
                incompleteMethods.removeIf(x -> {
                    x.resolve();
                    return true;
                });
            }
        }
    }

    /**
     * TODO
     */
    private void parsePendingResultMaps() {
        if (incompleteResultMaps.isEmpty()) {
            return;
        }
        synchronized (incompleteResultMaps) {
            boolean resolved;
            IncompleteElementException ex = null;
            do {
                resolved = false;
                Iterator<ResultMapResolver> iterator = incompleteResultMaps.iterator();
                while (iterator.hasNext()) {
                    try {
                        iterator.next().resolve();
                        iterator.remove();
                        resolved = true;
                    } catch (IncompleteElementException e) {
                        ex = e; // ex将存储最后一个e
                    }
                }
                // 如果每一次resolve都不成功, 就继续循环
                // 重试
            } while (resolved);
            if (!incompleteResultMaps.isEmpty() && ex != null) {
                // 至少有一个结果映射是无法resolve的。
                throw ex;
            }
        }
    }


    /**
     * @see MapperRegistry#addMappers(String)
     */
    public void addMappers(String packageName) {
        mapperRegistry.addMappers(packageName);
    }
}
