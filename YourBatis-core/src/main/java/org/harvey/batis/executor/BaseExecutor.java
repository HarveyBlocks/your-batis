package org.harvey.batis.executor;

import org.harvey.batis.cache.CacheKey;
import org.harvey.batis.cache.PerpetualCache;
import org.harvey.batis.config.Configuration;
import org.harvey.batis.cursor.Cursor;
import org.harvey.batis.exception.UnfinishedFunctionException;
import org.harvey.batis.exception.executor.ExecutorException;
import org.harvey.batis.executor.param.DefaultParameterHandler;
import org.harvey.batis.executor.result.DefaultMapResultHandler;
import org.harvey.batis.executor.result.DefaultResultHandler;
import org.harvey.batis.executor.result.ResultExtractor;
import org.harvey.batis.executor.result.ResultHandler;
import org.harvey.batis.io.log.Log;
import org.harvey.batis.io.log.LogFactory;
import org.harvey.batis.io.log.jdbc.ConnectionLogger;
import org.harvey.batis.mapping.BoundSql;
import org.harvey.batis.mapping.Environment;
import org.harvey.batis.mapping.MappedStatement;
import org.harvey.batis.mapping.ParameterMapping;
import org.harvey.batis.reflection.MetaObject;
import org.harvey.batis.reflection.factory.ObjectFactory;
import org.harvey.batis.session.RowBounds;
import org.harvey.batis.transaction.Transaction;
import org.harvey.batis.util.ErrorContext;
import org.harvey.batis.util.enums.ParameterMode;
import org.harvey.batis.util.enums.StatementType;
import org.harvey.batis.util.type.TypeHandlerRegistry;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-14 16:52
 */
public abstract class BaseExecutor implements Executor {


    private static final Log LOG = LogFactory.getLog(BaseExecutor.class);
    public static final String LOCAL_CACHE_ID = "LocalCache";
    public static final String LOCAL_OUTPUT_PARAMETER_CACHE_ID = "LocalOutputParameterCache";


    protected Configuration configuration;

    protected Transaction transaction;
    /**
     * 本地缓存, 每一次查询都要一个本地缓存, 查询结束后清理对应缓存<br>
     * 缓存的作用是为了应对嵌套的ParamMap<br>
     * 嵌套的ParamMap可能会需要多次查询, 那么就需要在这里存储上一次查询的数据<br>
     * 但是一个ParamMap如果对应一次完整的查询, 那么, 多次完整查询之前都会清除数据<br>
     */
    private PerpetualCache localCache = new PerpetualCache(LOCAL_CACHE_ID);
    private PerpetualCache localOutputParameterCache = new PerpetualCache(LOCAL_OUTPUT_PARAMETER_CACHE_ID);
    private ConcurrentLinkedQueue<DeferredLoad> deferredLoads = new ConcurrentLinkedQueue<>();
    protected Executor wrapper = this;
    private boolean closed = false;
    /**
     * TODO 似乎是查询的栈深度, 因为存在嵌套的ParamMap的缘故
     */
    protected int queryStack = 0;

    protected BaseExecutor(Configuration configuration, Transaction transaction) {
        this.transaction = transaction;
        this.configuration = configuration;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public Transaction getTransaction() {
        if (closed) {
            throw new ExecutorException("Executor was closed.");
        }
        return transaction;
    }

    @Override
    public void close(boolean forceRollback) {
        try {
            try {
                // 先尝试回滚
                rollback(forceRollback);
            } finally {
                // 再尝试关闭事务
                if (transaction != null) {
                    transaction.close();
                }
            }
        } catch (SQLException e) {
            // Ignore. There's nothing that can be done at this point.
            LOG.warn("Unexpected exception on closing transaction.  Cause: " + e);
        } finally {
            transaction = null;
            deferredLoads = null;
            localCache = null;
            localOutputParameterCache = null;
            closed = true;
        }
    }


    @Override
    public int update(MappedStatement ms, Object parameter) throws SQLException {
        ErrorContext.instance().setResource(ms.getResource()).setActivity("executing an update").setObject(ms.getId());
        if (closed) {
            throw new ExecutorException("Executor was closed.");
        }
        clearLocalCache();
        return doUpdate(ms, parameter);
    }

    @Override
    public void clearLocalCache() {
        if (!closed) {
            localCache.clear();
            localOutputParameterCache.clear();
        }
    }

    @Override
    public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType) {
        if (closed) {
            throw new ExecutorException("Executor was closed.");
        }
        DeferredLoad deferredLoad = new DeferredLoad(resultObject, property, key, localCache, configuration, targetType);
        if (deferredLoad.canLoad()) {
            // 可加载
            deferredLoad.load();
        } else {
            // 不可加载
            deferredLoads.add(deferredLoad);
        }
    }

    protected void closeStatement(Statement statement) {
        if (statement == null) {
            return;
        }
        try {
            statement.close();
        } catch (SQLException e) {
            // ignore
        }
    }

    protected Connection getConnection(Log statementLog) throws SQLException {
        Connection connection = transaction.getConnection();
        return statementLog.isDebugEnabled() ?
                ConnectionLogger.newInstance(connection, statementLog, queryStack) :
                connection;

    }

    /**
     * 利用枚举是单例的特性来实现唯一标识<br>
     * Execution用占位符
     */
    protected enum ExecutionPlaceholder {
        SINGLETON
    }


    /**
     * 延迟加载
     */
    private final static class DeferredLoad {
        private final MetaObject resultObject;
        private final String property;
        private final Class<?> targetType;
        private final CacheKey key;
        private final PerpetualCache localCache;
        private final ResultExtractor resultExtractor;

        public DeferredLoad(MetaObject resultObject,
                            String property,
                            CacheKey key,
                            PerpetualCache localCache,
                            Configuration configuration,
                            Class<?> targetType) {
            this.resultObject = resultObject;
            this.property = property;
            this.key = key;
            this.localCache = localCache;
            ObjectFactory factory = configuration.getObjectFactory();
            this.resultExtractor = new ResultExtractor(configuration, factory);
            this.targetType = targetType;
        }

        /**
         * @return 用{@link #key}获取的对象不是null且<br>
         * 不是{@link ExecutionPlaceholder#SINGLETON}占位符则为true
         */
        public boolean canLoad() {
            Object object = localCache.getObject(key);
            return object != null && object != ExecutionPlaceholder.SINGLETON;
        }

        /**
         * 从{@link #localCache}中依据{@link #key}获取资源, 然后存入{@link #resultObject}
         */
        public void load() {
            List<Object> list = (List<Object>) localCache.getObject(key);
            Object value = resultExtractor.extractFromList(list, targetType);
            resultObject.setValue(property, value);
        }
    }

    /**
     * isRollBack=false
     *
     * @see #flushStatements(boolean)
     */
    @Override
    public List<BatchResult> flushStatements() throws SQLException {
        return flushStatements(false);
    }

    /**
     * @param isRollBack 是否是再rollback的时候调用该方法
     * @return {@link Executor#flushStatements()}
     */
    public List<BatchResult> flushStatements(boolean isRollBack) throws SQLException {
        if (closed) {
            throw new ExecutorException("Executor was closed.");
        }
        return doFlushStatements(isRollBack);
    }


    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler<?> resultHandler) throws SQLException {
        BoundSql boundSql = ms.getBoundSql(parameter);
        CacheKey key = this.createCacheKey(ms, parameter, rowBounds, boundSql);
        return query(ms, parameter, rowBounds, resultHandler, key, boundSql);
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler<?> resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
        // 记录异常日志
        ErrorContext.instance().setResource(ms.getResource()).setActivity("executing a query").setObject(ms.getId());
        if (closed) {
            // 若已经关闭连接就抛出异常
            throw new ExecutorException("Executor was closed.");
        }
        if (queryStack == 0 && ms.isFlushCacheRequired()) {
            // 第一层查询, 且需要查询, 此时清空本地缓存
            this.clearLocalCache();
        }

        List<E> list = null;
        try {
            queryStack++;
            if (resultHandler == null) {
                // 从缓存中取出之前查询过的数据
                list = (List<E>) localCache.getObject(key);
            }
            if (list != null) {
                // 存在数据, 说明是嵌套的查询
                // 处理本地缓存的输出参数
                this.handleLocallyCachedOutputParameters(ms, key, parameter, boundSql);
            } else {
                // 从数据库查询数据
                list = this.queryFromDatabase(ms, parameter, rowBounds, resultHandler, key, boundSql);
            }
        } finally {
            queryStack--;
        }
        return list;
    }

    /**
     * ms在在{@link StatementType#CALLABLE}的情况下可用<br>
     * 处理本地缓存的输出参数<br>
     * 依据key从缓存中取出数据, 然后注入parameter中
     *
     * @param ms        映射语句, 映射关系是Mapper接口中的方法和XML文件中的SQL语句
     * @param parameter 参数, 用于向SQL语句中的?填入具体的值
     * @param key       从缓存中获取缓存内容的键, 依据参数保证唯一性{@link CacheKey}
     * @param boundSql  TODO
     */
    private void handleLocallyCachedOutputParameters(MappedStatement ms, CacheKey key, Object parameter, BoundSql boundSql) {
        if (ms.getStatementType() != StatementType.CALLABLE) {
            return;
        }
        // 启用了存储功能
        // 取出存储的东西, 这些数据将被注入parameter
        final Object cachedParameter = localOutputParameterCache.getObject(key);
        if (cachedParameter == null || parameter == null) {
            return;
        }
        // 获取值的源
        final MetaObject metaCachedParameter = configuration.newMetaObject(cachedParameter);
        // 需要被注入的目标
        final MetaObject metaParameter = configuration.newMetaObject(parameter);
        for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
            if (parameterMapping.getMode() == ParameterMode.IN) {
                continue;
            }
            // parameterMapping的属性是OUT or INOUT
            // 获取目标属性
            final String parameterName = parameterMapping.getProperty();
            // 取出数据
            final Object cachedValue = metaCachedParameter.getValue(parameterName);
            // 反射注入
            metaParameter.setValue(parameterName, cachedValue);
        }
    }


    /**
     * 从数据库查询数据
     *
     * @param ms            映射语句, 映射关系是Mapper接口中的方法和XML文件中的SQL语句
     * @param parameter     参数, 用于向SQL语句中的?填入具体的值
     * @param rowBounds     上下限
     * @param resultHandler 结果从jdbcType到JavaType的映射, 见{@link ResultHandler}. <br>
     *                      一般是{@link DefaultResultHandler}和{@link DefaultMapResultHandler}
     * @param key           从缓存中获取缓存内容的键, 依据参数保证唯一性{@link CacheKey}
     * @param boundSql      TODO
     * @param <E>           返回的结果类型
     * @return 从数据库中查询, 然后返回查询到的结果
     * @throws SQLException 查询过程中出现的异常
     */
    private <E> List<E> queryFromDatabase(MappedStatement ms, Object parameter,
                                          RowBounds rowBounds, ResultHandler<?> resultHandler,
                                          CacheKey key, BoundSql boundSql) throws SQLException {
        List<E> list;
        // 要准备开始查询了, 先放一个占位符
        localCache.putObject(key, ExecutionPlaceholder.SINGLETON);
        try {
            // 执行查询
            list = doQuery(ms, parameter, rowBounds, resultHandler, boundSql);
        } finally {
            // 查完之后从缓存释放占位符
            localCache.removeObject(key);
        }
        // 放入查询结果
        localCache.putObject(key, list);
        if (ms.getStatementType() == StatementType.CALLABLE) {
            // 只有在StatementType为Callback时才存入
            localOutputParameterCache.putObject(key, parameter);
        }
        return list;
    }

    @Override
    public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException {
        throw new UnfinishedFunctionException("暂无Cursor");
    }

    /**
     * @see Transaction#commit()
     */
    @Override
    public void commit(boolean required) throws SQLException {
        if (closed) {
            throw new ExecutorException("Cannot commit, transaction is already closed");
        }
        // 清空本地缓存
        clearLocalCache();
        flushStatements();
        if (required) {
            transaction.commit();
        }
    }

    /**
     * @see Transaction#rollback()
     */
    @Override
    public void rollback(boolean required) throws SQLException {
        if (closed) {
            return;
        }
        try {
            // 清空本地缓存
            clearLocalCache();
            flushStatements(true);
        } finally {
            if (required) {
                transaction.rollback();
            }
        }
    }

    /**
     * 依据参数来不断给Cache的值进行更新
     *
     * @param ms              有关SQL的特征, 用于构建key
     * @param rowBounds       有关SQL的特征, 用于构建key
     * @param boundSql        解析XML后得到SQL封装, 有对Param的需求
     * @param parameterObject 从Mapper代理中来, 保存有具体的Mapper方法的值,
     *                        依靠对Param的需求, 从中取出参数值, 该值将用于构建KEY
     * @return 构建之后的KEY
     */
    @Override
    public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
        if (closed) {
            throw new ExecutorException("Executor was closed.");
        }
        CacheKey cacheKey = new CacheKey();
        cacheKey.update(ms.getId());
        cacheKey.update(rowBounds.getOffset());
        cacheKey.update(rowBounds.getLimit());
        cacheKey.update(boundSql.getSql());
        // 从XML解析出来的需要的参数
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        TypeHandlerRegistry typeHandlerRegistry = ms.getConfiguration().getTypeHandlerRegistry();
        // 用Mapper方法的参数进行对Key构建
        for (ParameterMapping parameterMapping : parameterMappings) {
            if (parameterMapping.getMode() == ParameterMode.OUT) {
                continue;
            }
            // 依据反射代理, 从Parameter中获取值(调用代理Mapper方法时的参数的值)
            String propertyName = parameterMapping.getProperty();
            Object value = getPorpertyFromParameterObject(parameterObject, propertyName, boundSql, typeHandlerRegistry);
            cacheKey.update(value);
        }
        Environment environment = configuration.getEnvironment();
        if (environment != null) {
            cacheKey.update(environment.getId());
        }
        return cacheKey;
    }

    private Object getPorpertyFromParameterObject(
            Object parameterObject, String propertyName, BoundSql boundSql,
            TypeHandlerRegistry typeHandlerRegistry) {
        return DefaultParameterHandler.getPorpertyFromParameterObject(
                propertyName, boundSql, typeHandlerRegistry, configuration, parameterObject);
    }

    /**
     * @return {@link #localCache} 内含有key吗, 含有则返回true
     */
    @Override
    public boolean isCached(MappedStatement ignored, CacheKey key) {
        return localCache.getObject(key) != null;
    }

    @Override
    public void setExecutorWrapper(Executor wrapper) {
        this.wrapper = wrapper;
    }

    protected abstract int doUpdate(MappedStatement ms, Object parameter) throws SQLException;

    /**
     * 不用担心是否关闭, 一定没关闭
     *
     * @param isRollback {@link #flushStatements(boolean)}
     * @return TODO
     * @throws SQLException 查询数据库时可能产生的异常, {@link SQLException}
     */
    protected abstract List<BatchResult> doFlushStatements(boolean isRollback) throws SQLException;

    protected abstract <E> List<E> doQuery(MappedStatement ms, Object parameter,
                                           RowBounds rowBounds, ResultHandler<?> resultHandler,
                                           BoundSql boundSql)
            throws SQLException;
}
