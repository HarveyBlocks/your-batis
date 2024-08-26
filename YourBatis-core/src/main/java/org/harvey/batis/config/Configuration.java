package org.harvey.batis.config;

import lombok.Getter;
import lombok.Setter;
import org.harvey.batis.binding.MapperRegistry;
import org.harvey.batis.builder.CacheRefResolver;
import org.harvey.batis.builder.MethodResolver;
import org.harvey.batis.builder.ResultMapResolver;
import org.harvey.batis.builder.xml.XMLStatementBuilder;
import org.harvey.batis.exception.UnfinishedFunctionException;
import org.harvey.batis.exception.builder.IncompleteElementException;
import org.harvey.batis.executor.Executor;
import org.harvey.batis.executor.SimpleExecutor;
import org.harvey.batis.executor.key.generator.KeyGenerator;
import org.harvey.batis.executor.key.generator.SelectKeyGenerator;
import org.harvey.batis.executor.param.ParameterHandler;
import org.harvey.batis.executor.result.DefaultResultSetHandler;
import org.harvey.batis.executor.result.ResultHandler;
import org.harvey.batis.executor.result.ResultSetHandler;
import org.harvey.batis.executor.statement.RoutingStatementHandler;
import org.harvey.batis.executor.statement.StatementHandler;
import org.harvey.batis.mapping.*;
import org.harvey.batis.parsing.XNode;
import org.harvey.batis.plugin.Interceptor;
import org.harvey.batis.plugin.InterceptorChain;
import org.harvey.batis.reflection.DefaultReflectorFactory;
import org.harvey.batis.reflection.MetaObject;
import org.harvey.batis.reflection.ReflectorFactory;
import org.harvey.batis.reflection.factory.DefaultObjectFactory;
import org.harvey.batis.reflection.factory.ObjectFactory;
import org.harvey.batis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.harvey.batis.reflection.wrapper.ObjectWrapperFactory;
import org.harvey.batis.scripting.LanguageDriver;
import org.harvey.batis.scripting.LanguageDriverRegistry;
import org.harvey.batis.scripting.xml.XmlLanguageDriver;
import org.harvey.batis.session.RowBounds;
import org.harvey.batis.session.SqlSession;
import org.harvey.batis.transaction.Transaction;
import org.harvey.batis.util.StrictMap;
import org.harvey.batis.util.enums.AutoMappingBehavior;
import org.harvey.batis.util.enums.ExecutorType;
import org.harvey.batis.util.enums.JdbcType;
import org.harvey.batis.util.enums.ResultSetType;
import org.harvey.batis.util.type.TypeHandlerRegistry;
import org.harvey.batis.util.type.UnknownTypeHandler;

import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * TODO
 * 管理中心, 许多类需要被本来管理, 这些类就作为本类的字段, 由本类实例化<br>
 * 这些被管理的类需要直到自己的管理中心是哪个对象, 就将本类作为构造器的参数
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-02 13:28
 */
@Getter
@Setter
public class Configuration {
    protected Environment environment;

    protected boolean useActualParamName = true;
    /**
     * 一些数据库ColumnLabel和ColumnName的概念可能不一致
     *
     * @see ResultSetMetaData#getColumnLabel(int) sql语句中指定的名称(字段名或别名)
     * @see ResultSetMetaData#getColumnName(int) 只能取到查询的数据库表的字段名称,而不是sql语句中用到的别名
     */
    protected boolean useColumnLabel = true;
    protected boolean useGeneratedKeys = false;
    private boolean cacheEnabled = false;
    private boolean lazyLoadingEnabled = false;
    private boolean returnInstanceForEmptyRow = false;
    private boolean callSettersOnNulls = false;
    private boolean shrinkWhitespacesInSql = true;


    protected ExecutorType defaultExecutorType = ExecutorType.SIMPLE;
    protected JdbcType jdbcTypeForNull = JdbcType.OTHER;
    @Getter
    protected AutoMappingBehavior autoMappingBehavior = AutoMappingBehavior.PARTIAL;

    protected ResultSetType defaultResultSetType = null;
    protected Integer defaultFetchSize = null;
    protected Integer defaultStatementTimeout = null;
    // ...

    protected Properties variables = new Properties();
    protected ObjectFactory objectFactory = new DefaultObjectFactory();

    private final UnknownTypeHandler unknownTypeHandler;
    protected ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();
    // ...

    /**
     * 可以添加各种Interceptor, 然后对一些类的一些方法进行增强<br>
     * 其中, {@link ParameterHandler}, {@link ResultSetHandler},
     * {@link StatementHandler}, {@link Executor} 可以被增强
     */

    protected final InterceptorChain interceptorChain = new InterceptorChain();
    protected final MapperRegistry mapperRegistry = new MapperRegistry(this);
    protected final LanguageDriverRegistry languageRegistry = new LanguageDriverRegistry();
    protected final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry(this);


    protected final Map<String, ResultMap> resultMaps = new StrictMap<>("Result Maps collection");
    /**
     * TODO
     */
    protected final Map<String, ParameterMap> parameterMaps = new StrictMap<>("Parameter Maps collection");
    protected final Map<String, KeyGenerator> keyGenerators = new StrictMap<>("Key Generators collection");
    /**
     * Mapper.xml的资源
     */
    protected final Set<String> loadedResources = new HashSet<>();

    /**
     * TODO
     */
    protected final Map<String, XNode> sqlFragments = new StrictMap<>("XML fragments parsed from previous mappers");
    /**
     * TODO
     */
    protected final Map<String, MappedStatement> mappedStatements = new StrictMap<MappedStatement>("Mapped Statements collection").conflictMessageProducer((savedValue, targetValue) -> ". please check " + savedValue.getResource() + " and " + targetValue.getResource());

    protected ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
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
     * 暂无需求
     */
    public Configuration() {
        this.unknownTypeHandler = new UnknownTypeHandler(this);
        languageRegistry.setDefaultDriverClass(XmlLanguageDriver.class);
        UnfinishedFunctionException.trace("注册别名");
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
     * @param statementName 转变成{@link #mappedStatements}的Key, 有个规范<br>
     *                      {@code type.getName() + "." + method.getName();}
     * @see #mappedStatements
     * @see HashMap#containsKey(Object)
     */
    public boolean hasStatement(String statementName) {
        return this.hasStatement(statementName, true);
    }

    /**
     * @param statementName                转变成{@link #mappedStatements}的Key, 有个规范<br>
     *                                     {@code type.getName() + "." + method.getName();}
     * @param validateIncompleteStatements 验证不完整的语句,{@link #buildAllStatements()}
     * @see #mappedStatements
     * @see HashMap#containsKey(Object)
     */
    public boolean hasStatement(String statementName, boolean validateIncompleteStatements) {
        if (validateIncompleteStatements) {
            this.buildAllStatements();
        }
        return mappedStatements.containsKey(statementName);
    }

    /**
     * @param id 转变成{@link #mappedStatements}的Key, 有个规范<br>
     *           {@code type.getName() + "." + method.getName();}
     * @see #mappedStatements
     * @see HashMap#get(Object)
     */
    public MappedStatement getMappedStatement(String id) {
        return this.getMappedStatement(id, true);
    }

    /**
     * @param id                           转变成{@link #mappedStatements}的Key, 有个规范<br>
     *                                     {@code type.getName() + "." + method.getName();}
     * @param validateIncompleteStatements 验证未完成的Statement,{@link #buildAllStatements()}
     * @see #mappedStatements
     * @see HashMap#get(Object)
     */
    public MappedStatement getMappedStatement(String id, boolean validateIncompleteStatements) {
        if (validateIncompleteStatements) {
            this.buildAllStatements();
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
        this.parsePendingRemoveFinished(incompleteResultMaps, ResultMapResolver::resolve, true);
        this.parsePendingCollection(incompleteCacheRefs, item -> item.resolveCacheRef() != null);
        this.parsePendingCollection(incompleteStatements, item -> {
            item.parseStatementNode();
            return true;
        });
        this.parsePendingCollection(incompleteMethods, item -> {
            item.resolve();
            return true;
        });
    }

    private <T> void parsePendingCollection(Collection<T> c, Function<T, Boolean> resolver) {
        if (c.isEmpty()) {
            return;
        }
        synchronized (c) {
            // 将未处理的集合元素全部处理掉, 然后集合中删除完成处理的元素
            c.removeIf(resolver::apply);
        }
    }

    /**
     * 解析未完成的ResultMaps/Statement
     */
    public <T> void parsePendingRemoveFinished(Collection<T> c, Consumer<T> phase, boolean throwIncomplete) {
        Iterator<T> it = c.iterator();
        synchronized (c) { // 这一次, 上锁再次解析
            while (it.hasNext()) {
                try {
                    phase.accept(it.next());
                    it.remove();
                } catch (IncompleteElementException e) {
                    if (throwIncomplete) {
                        throw e;
                    }
                }
            }
        }
    }

    /**
     * @see MapperRegistry#addMappers(String)
     */
    public void addMappers(String packageName) {
        mapperRegistry.addMappers(packageName);
    }

    /**
     * TODO
     *
     * @deprecated 似乎是为了应对Spring
     */
    @Deprecated
    public boolean isResourceLoaded(String resource) {
        return loadedResources.contains(resource);
    }

    /**
     * TODO
     *
     * @deprecated 似乎是为了应对Spring
     */
    @Deprecated
    public void addLoadedResource(String resource) {
        loadedResources.add(resource);
    }

    /**
     * TODO
     *
     * @param resultMapResolver {@link #incompleteResultMaps}
     */
    public void addIncompleteResultMap(ResultMapResolver resultMapResolver) {
        this.incompleteResultMaps.add(resultMapResolver);
    }

    /**
     * TODO
     *
     * @param incompleteStatement {@link #incompleteStatements}
     * @see XMLStatementBuilder
     */
    public void addIncompleteStatement(XMLStatementBuilder incompleteStatement) {
        incompleteStatements.add(incompleteStatement);
    }


    public LanguageDriver getLanguageDriver() {
        return languageRegistry.getDefaultDriver();
    }

    public MappedStatement addMappedStatement(MappedStatement ms) {
        return mappedStatements.put(ms.getId(), ms);
    }

    public LanguageDriver getDefaultScriptingLanguageInstance() {
        return languageRegistry.getDefaultDriver();
    }

    public void addResultMap(ResultMap resultMap) {
        resultMaps.put(resultMap.getId(), resultMap);
        // 在本地检查区分的嵌套结果映射
        // checkLocallyForDiscriminatedNestedResultMaps(resultMap);
        // 检查全局用于区分嵌套结果映射
        // checkGloballyForDiscriminatedNestedResultMaps(resultMap);
    }

    public ResultMap getResultMap(String id) {
        return resultMaps.get(id);
    }

    public ParameterMap getParameterMap(String id) {
        return parameterMaps.get(id);
    }

    /**
     * @param id fullClassName+methodName+prefix
     * @see SelectKeyGenerator#SELECT_KEY_SUFFIX
     */
    public boolean hasKeyGenerator(String id) {
        return keyGenerators.containsKey(id);
    }

    /**
     * @param id fullClassName+methodName+prefix
     * @see SelectKeyGenerator#SELECT_KEY_SUFFIX
     */
    public void addKeyGenerator(String id, KeyGenerator keyGenerator) {
        keyGenerators.put(id, keyGenerator);
    }

    /**
     * @param id fullClassName+methodName+prefix
     * @see SelectKeyGenerator#SELECT_KEY_SUFFIX
     */
    public KeyGenerator getKeyGenerator(String id) {
        return keyGenerators.get(id);
    }

    public MetaObject newMetaObject(Object object) {
        return MetaObject.forObject(object, objectFactory, objectWrapperFactory, reflectorFactory);
    }


    public boolean hasMapper(Class<?> type) {
        return mapperRegistry.hasMapper(type);
    }

    public <T> void addMapper(Class<T> type) {
        mapperRegistry.addMapper(type);
    }

    public void addInterceptor(Interceptor interceptor) {
        interceptorChain.addInterceptor(interceptor);
    }

    /**
     * @see ExecutorType
     * @see SimpleExecutor
     * @see #pluginInterceptor(Object)
     */
    public Executor newExecutor(Transaction tx, ExecutorType executorType) {
        executorType = executorType == null ? defaultExecutorType : executorType;
        executorType = executorType == null ? ExecutorType.SIMPLE : executorType;
        Executor executor;
        if (ExecutorType.BATCH == executorType) {
            throw new UnfinishedFunctionException("BATCH");
            // executor = new BatchExecutor(this, tx);
        } else if (ExecutorType.REUSE == executorType) {
            throw new UnfinishedFunctionException("REUSE");
            // executor = new ReuseExecutor(this, tx);
        } else {
            executor = new SimpleExecutor(this, tx);
        }
        if (cacheEnabled) {
            throw new UnfinishedFunctionException("cache");
            // executor = new CachingExecutor(executor);
        }
        return this.pluginInterceptor(executor);
    }

    /**
     * @see StatementHandler
     * @see RoutingStatementHandler
     * @see RoutingStatementHandler#RoutingStatementHandler(Executor, MappedStatement, Object, RowBounds, ResultHandler, BoundSql)
     * @see #pluginInterceptor(Object)
     */
    public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler<?> resultHandler, BoundSql boundSql) {
        StatementHandler statementHandler = new RoutingStatementHandler(executor, mappedStatement, parameterObject, rowBounds, resultHandler, boundSql);
        return this.pluginInterceptor(statementHandler);
    }

    /**
     * @see ParameterHandler
     * @see LanguageDriver
     * @see LanguageDriver#createParameterHandler(MappedStatement, Object, BoundSql)
     * @see #pluginInterceptor(Object)
     */
    public ParameterHandler newParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        ParameterHandler parameterHandler = mappedStatement.getLang().createParameterHandler(mappedStatement, parameterObject, boundSql);
        return this.pluginInterceptor(parameterHandler);
    }


    /**
     * @see DefaultResultSetHandler
     * @see DefaultResultSetHandler#DefaultResultSetHandler(Executor, MappedStatement, ParameterHandler, ResultHandler, BoundSql, RowBounds)
     * @see #pluginInterceptor(Object)
     */
    public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, RowBounds rowBounds, ParameterHandler parameterHandler, ResultHandler<?> resultHandler, BoundSql boundSql) {
        ResultSetHandler resultSetHandler = new DefaultResultSetHandler(executor, mappedStatement, parameterHandler, resultHandler, boundSql, rowBounds);
        return this.pluginInterceptor(resultSetHandler);
    }

    /**
     * 增强toBePlugin
     *
     * @see #interceptorChain
     * @see InterceptorChain
     * @see Interceptor
     */
    private <T> T pluginInterceptor(T toBePlugin) {
        return (T) interceptorChain.pluginAll(toBePlugin);
    }


}
