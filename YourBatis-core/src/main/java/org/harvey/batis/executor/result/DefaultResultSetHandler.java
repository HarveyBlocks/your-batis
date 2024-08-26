package org.harvey.batis.executor.result;

import org.harvey.batis.cache.CacheKey;
import org.harvey.batis.config.Configuration;
import org.harvey.batis.cursor.Cursor;
import org.harvey.batis.exception.UnfinishedFunctionException;
import org.harvey.batis.exception.executor.ExecutorException;
import org.harvey.batis.executor.Executor;
import org.harvey.batis.executor.loader.ResultLoaderMap;
import org.harvey.batis.executor.param.ParameterHandler;
import org.harvey.batis.mapping.BoundSql;
import org.harvey.batis.mapping.MappedStatement;
import org.harvey.batis.mapping.ResultMap;
import org.harvey.batis.mapping.ResultMapping;
import org.harvey.batis.reflection.MetaClass;
import org.harvey.batis.reflection.MetaObject;
import org.harvey.batis.reflection.ReflectorFactory;
import org.harvey.batis.reflection.factory.ObjectFactory;
import org.harvey.batis.session.RowBounds;
import org.harvey.batis.util.ErrorContext;
import org.harvey.batis.util.enums.AutoMappingBehavior;
import org.harvey.batis.util.type.TypeHandler;
import org.harvey.batis.util.type.TypeHandlerRegistry;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-20 22:40
 */
public class DefaultResultSetHandler implements ResultSetHandler {
    /**
     * 递 延
     */
    private static final Object DEFERRED = new Object();
    private final Executor executor;
    private final Configuration configuration;
    private final MappedStatement mappedStatement;
    private final RowBounds rowBounds;
    private final ParameterHandler parameterHandler;
    private final ResultHandler<?> resultHandler;
    private final BoundSql boundSql;
    private final TypeHandlerRegistry typeHandlerRegistry;
    private final ObjectFactory objectFactory;
    private final ReflectorFactory reflectorFactory;

    /**
     * 嵌套 resultMaps
     */
    private final Map<CacheKey, Object> nestedResultObjects = new HashMap<>();

    /**
     * 临时标记, 若要使用构造函数映射(使用字, 段减少内存占用), 则为true
     */
    private boolean useConstructorMappings;

    public DefaultResultSetHandler(Executor executor, MappedStatement mappedStatement, ParameterHandler parameterHandler, ResultHandler<?> resultHandler, BoundSql boundSql, RowBounds rowBounds) {
        this.executor = executor;
        this.configuration = mappedStatement.getConfiguration();
        this.mappedStatement = mappedStatement;
        this.rowBounds = rowBounds;
        this.parameterHandler = parameterHandler;
        this.boundSql = boundSql;
        this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        this.objectFactory = configuration.getObjectFactory();
        this.reflectorFactory = configuration.getReflectorFactory();
        this.resultHandler = resultHandler;
    }


    /**
     * 处理结果集
     *
     * @return 如果只有一个结果, 那就返回[col1,col2]<br>
     * 如果有多个结果集, 那就返回[[col1,col2],[col1],[col3,col4]]
     */
    @Override
    public List<Object> handleResultSets(Statement stmt) throws SQLException {
        ErrorContext.instance().setActivity("handling results").setObject(mappedStatement.getId());
        // 获取ResultSetWrapper和resultMaps
        ResultSetWrapper rsw = this.getFirstResultSet(stmt);
        List<ResultMap> resultMaps = mappedStatement.getResultMaps();
        int resultMapCount = resultMaps.size();
        this.validateResultMapsCount(rsw, resultMapCount);

        // 保存所有的ResultSet和ResultMap都解析完了之后的结果
        final List<Object> multipleResults = new ArrayList<>();
        // JDBC的ResultSet应该是和XML配置的ResultMap应该是一一对应的

        int resultSetCount = 0;
        // 先遍历ResultMap
        for (; rsw != null && resultSetCount < resultMapCount; resultSetCount++) {
            // 遍历resultMaps
            ResultMap resultMap = resultMaps.get(resultSetCount);
            // 这个resultMap是和上一个rsw对应的
            this.handleResultSet(rsw, resultMap, multipleResults, null);
            rsw = this.getNextResultSet(stmt); // 获取下一个ResultSet
            this.cleanUpAfterHandlingResultSet();
        }
        // 此时rsw==null(表示resultSet已经遍历完)
        // 或 resultSetCount>=resultMapCount(表示resultMap已经遍历完)
        // 如果两个条件同时满足(同时遍历完), 皆大欢喜
        // 如果两个条件只有一个满足, 那不太好啊
        // 那么只会取其中少的那个
        String[] resultSets = mappedStatement.getResultSets();
        if (resultSets != null) {
            throw new UnfinishedFunctionException();
        }
        // 折叠单一结果列表
        return multipleResults.size() == 1 ? (List<Object>) multipleResults.get(0) : multipleResults;
    }


    /**
     * 由于Statement里可能有多个SQL语句<br>
     * 那就有多个ResultSet<br>
     * SQL可能是读, 可能是写<br>
     * 写的话, <pre>{@code
     * statement.getUpdateCount()!=-1 && statement.getResultSet()==null
     * }</pre>
     * 读的话, <pre>{@code
     * statement.getUpdateCount()==-1 && statement.getResultSet()!=null
     * }</pre>
     * 反复调用{@link Statement#getResultSet()}直到不为null
     *
     * @see Statement#getMoreResults()
     * @see Statement#getResultSet()
     * @see Statement#getUpdateCount()
     */
    private ResultSetWrapper getFirstResultSet(Statement stmt) throws SQLException {
        ResultSet rs = stmt.getResultSet();
        while (rs == null) {
            // 如果driver未将ResultSet作为第一个结果返回
            // 则寻找下一个以获取第一个ResultSet
            // stmt.getMoreResults()
            // 移动指针, 指向以下一个resultSet
            // 返回是否有resultSet, 有则返回true
            if (stmt.getMoreResults()) {
                // 调用了getMoreResults之后调用getResultSet
                // 获取新resultSet
                rs = stmt.getResultSet();
                // 直到rs不为null
                continue;
            }
            if (stmt.getUpdateCount() == -1) {
                // 不是写操作, 但是没有MoreResultSet
                break;
            }
            // 本语句是写操作, 再查看下一条
        }
        return rs == null ? null : new ResultSetWrapper(rs, configuration);
    }


    /**
     * 认为如果存在{@link ResultSetWrapper}, 就必须要有resultMap
     */
    private void validateResultMapsCount(ResultSetWrapper rsw, int resultMapCount) {
        if (rsw != null && resultMapCount < 1) {
            throw new ExecutorException("A query was run and no Result Maps were found for the Mapped Statement '" + mappedStatement.getId() + "'.  It's likely that neither a Result Type nor a Result Map was specified.");
        }
    }


    /**
     * 处理每一个resultSet, 然后存入参数multipleResults中
     *
     * @param rsw           resultSet
     * @param resultMap     Java字段和数据库字段映射关系
     * @param parentMapping TODO
     * @see #handleRowValues(ResultSetWrapper, ResultMap, ResultHandler, RowBounds, ResultMapping)
     */
    private void handleResultSet(ResultSetWrapper rsw, ResultMap resultMap, List<Object> multipleResults, ResultMapping parentMapping) throws SQLException {
        try {
            if (parentMapping != null) {
                // parentMapping不为null
                // 采用parentMapping
                this.handleRowValues(rsw, resultMap, null, RowBounds.DEFAULT, parentMapping);
                return;
            }
            if (resultHandler != null) {
                // parentMapping为null
                // 本类字段resultHandler不为null, 采用本类字段resultHandler
                this.handleRowValues(rsw, resultMap, resultHandler, rowBounds, null);
                return;
            }
            DefaultResultHandler defaultResultHandler = new DefaultResultHandler(objectFactory);
            this.handleRowValues(rsw, resultMap, defaultResultHandler, rowBounds, null);
            multipleResults.add(defaultResultHandler.getResultList());
        } finally {
            // close resultSets
            DefaultResultSetHandler.closeResultSet(rsw.getResultSet());
        }
    }

    /**
     * @see #handleRowValuesForSimpleResultMap(ResultSetWrapper, ResultMap, ResultHandler, RowBounds, ResultMapping)
     */
    public void handleRowValues(ResultSetWrapper rsw, ResultMap resultMap, ResultHandler<?> resultHandler, RowBounds rowBounds, ResultMapping parentMapping) throws SQLException {
        if (resultMap.isHasNestedResultMaps()) {
            throw new UnfinishedFunctionException();
            /*ensureNoRowBounds();
            checkResultHandler();
            handleRowValuesForNestedResultMap(rsw, resultMap, resultHandler, rowBounds, parentMapping);
            */
        } else {
            this.handleRowValuesForSimpleResultMap(rsw, resultMap, resultHandler, rowBounds, parentMapping);
        }
    }

    /**
     * @param rsw           ResultSet结果集
     * @param resultMap     数据库-Java类映射关系
     * @param resultHandler 结果处理其
     * @param rowBounds     TODO
     * @param parentMapping TODO
     */
    private void handleRowValuesForSimpleResultMap(
            ResultSetWrapper rsw, ResultMap resultMap,
            ResultHandler<?> resultHandler, RowBounds rowBounds,
            ResultMapping parentMapping) throws SQLException {
        // 创建一个
        DefaultResultContext<Object> resultContext = new DefaultResultContext<>();
        ResultSet resultSet = rsw.getResultSet();
        this.skipRows(resultSet, rowBounds);
        while (shouldProcessMoreRows(resultContext, rowBounds) && // context没有关闭, 且没有超过rowBounds
                !resultSet.isClosed() && // 结果集未关闭
                resultSet.next() // 还结果集还存在内容(同时移动指针, 同PreparedStatement获取ResultSet)
        ) {
            // 解析可区分(?)的ResultMap
            ResultMap discriminatedResultMap = this.resolveDiscriminatedResultMap(
                    resultSet, resultMap, null);
            // 获取一条记录对应的ResultMap
            Object rowValue = this.getRowValue(rsw, discriminatedResultMap, null);
            this.storeObject(resultHandler, resultContext, rowValue, parentMapping, resultSet);
        }
    }


    /**
     * 调整, 移动{@link ResultSet}到{@link RowBounds#getOffset()}
     */
    private void skipRows(ResultSet rs, RowBounds rowBounds) throws SQLException {
        if (rs.getType() != ResultSet.TYPE_FORWARD_ONLY) {
            // ResultSet.TYPE_SCROLL_INSENSITIVE // 滚动不敏感
            // ResultSet.TYPE_SCROLL_SENSITIVE // 滚动敏感
            if (rowBounds.getOffset() != RowBounds.NO_ROW_OFFSET) {
                // 直接向后移动offset
                rs.absolute(rowBounds.getOffset());
            }
            return;
        }
        // rs对结果的指针要被逐个移动
        for (int i = 0; i < rowBounds.getOffset(); i++) {
            if (!rs.next()) {
                break;
            }
        }
    }


    /**
     * Discriminator TODO
     */
    public ResultMap resolveDiscriminatedResultMap(ResultSet rs, ResultMap resultMap, String columnPrefix) throws SQLException {
        Set<String> pastDiscriminators = new HashSet<>();
        UnfinishedFunctionException.trace("Discriminator");
        // Discriminator discriminator = resultMap.getDiscriminator();
        /* TODO Discriminator
        while (discriminator != null) {
            final Object value = getDiscriminatorValue(rs, discriminator, columnPrefix);
            final String discriminatedMapId = discriminator.getMapIdFor(String.valueOf(value));
            if (!configuration.hasResultMap(discriminatedMapId)) {
                break;
            }
            resultMap = configuration.getResultMap(discriminatedMapId);
            Discriminator lastDiscriminator = discriminator;
            discriminator = resultMap.getDiscriminator();
            if (discriminator == lastDiscriminator || !pastDiscriminators.add(discriminatedMapId)) {
                break;
            }
        }*/
        return resultMap;
    }

    /**
     * TODO
     * 从结果集中获取结果类型并返回
     *
     * @param rsw          结果集合
     * @param resultMap    结果类型字段映射关系
     * @param columnPrefix 字段前缀
     * @return 已经注入了值的结果对象(一个)
     */
    private Object getRowValue(ResultSetWrapper rsw, ResultMap resultMap, String columnPrefix) throws SQLException {
        final ResultLoaderMap lazyLoader = new ResultLoaderMap();
        // 确认一条记录对应的结果类型, 获取原始类型(如果没有TypeHandler, 值等待被注入)
        Object rowValue = this.createResultObject(rsw, resultMap, lazyLoader, columnPrefix);
        if (rowValue == null || this.hasTypeHandlerForResultObject(rsw, resultMap.getType())) {
            // 是依据TypeHandler做转换的, 已经认为是注入好的
            return rowValue;
        }
        // 注入
        // 如果是需要注入的值, 那就注入值
        final MetaObject metaObject = configuration.newMetaObject(rowValue); // 没注入类型的结果
        boolean foundValues = this.useConstructorMappings; //false
        if (this.shouldApplyAutomaticMappings(resultMap, false)) {
            // 可以使用自动映射功能
            foundValues = applyAutomaticMappings(rsw, resultMap, metaObject, columnPrefix) || foundValues;
            // false
        }
        foundValues = applyPropertyMappings(rsw, resultMap, metaObject, lazyLoader, columnPrefix) // true
                || foundValues;
        foundValues = lazyLoader.size() > 0 || foundValues;
        // 没有完成映射返回null
        return foundValues || configuration.isReturnInstanceForEmptyRow() ? rowValue : null;
    }

    /**
     * TODO
     *
     * @param rsw
     * @param resultMap
     * @param lazyLoader   TODO
     * @param columnPrefix
     * @return
     * @throws SQLException
     * @see #createResultObject(ResultSetWrapper, ResultMap, List, List, String)
     */
    private Object createResultObject(
            ResultSetWrapper rsw, ResultMap resultMap,
            ResultLoaderMap lazyLoader, String columnPrefix) throws SQLException {
        this.useConstructorMappings = false; // reset previous mapping result
        final List<Class<?>> constructorArgTypes = new ArrayList<>();
        final List<Object> constructorArgs = new ArrayList<>();
        Object resultObject = this.createResultObject(
                rsw, resultMap, constructorArgTypes, constructorArgs, columnPrefix);
        if (resultObject != null && !this.hasTypeHandlerForResultObject(rsw, resultMap.getType())) {
            // 不是有TypeHandler的情况, 却实例化了?
            // 此时需要对嵌套且懒加载的ResultMapping进行处理
            final List<ResultMapping> fieldMappings = resultMap.getFieldResultMappings();
            for (ResultMapping propertyMapping : fieldMappings) {
                // 遍历ResultMapping
                if (propertyMapping.getNestedQueryId() == null) {
                    continue;
                }
                if (!propertyMapping.isLazy()) {
                    continue;
                }
                /* 获取代理
                ???
                resultObject = configuration
                        .getProxyFactory()
                        .createProxy(resultObject, lazyLoader, configuration,
                                objectFactory, constructorArgTypes, constructorArgs);
                */
                throw new UnfinishedFunctionException();
            }
        }
        this.useConstructorMappings = resultObject != null // 成功创建出resultObject
                && !constructorArgTypes.isEmpty(); // 且没有使用constructorMappings
        return resultObject;
    }

    /**
     * @param rsw                 结果集
     * @param resultMap           结果JDBC-JAVA类型映射
     * @param constructorArgTypes ignored 用于有constructorMappings的情况
     * @param constructorArgs     ignored 用于有constructorMappings的情况
     * @param columnPrefix        column名前缀
     * @return 结果, 可能结果类型没有TypeHandler, 那需要被注入
     * @see ResultMap#getType()
     * @see #createPrimitiveResultObject(ResultSetWrapper, ResultMap, String)
     * @see ObjectFactory#create(Class)
     */
    private Object createResultObject(
            ResultSetWrapper rsw, ResultMap resultMap,
            List<Class<?>> constructorArgTypes, List<Object> constructorArgs,
            String columnPrefix) throws SQLException {
        final Class<?> resultType = resultMap.getType();
        final MetaClass metaType = MetaClass.forClass(resultType, reflectorFactory);
        final List<ResultMapping> constructorMappings = resultMap.getConstructorResultMappings();
        if (this.hasTypeHandlerForResultObject(rsw, resultType)) {
            // 只有一个返回值的简单类型的情况
            // 创建原始的ResultObject
            return this.createPrimitiveResultObject(rsw, resultMap, columnPrefix);
        } else if (!constructorMappings.isEmpty()) {
            // 有构造器映射的情况
            // return createParameterizedResultObject(rsw, resultType, constructorMappings, constructorArgTypes, constructorArgs, columnPrefix);
            throw new UnfinishedFunctionException(constructorArgTypes, constructorArgs);
        } else if (resultType.isInterface() || metaType.hasDefaultConstructor()) {
            // entity实体类的情况
            // 要求于数据库对应的实体类应该具有默认构造器
            return objectFactory.create(resultType);
        } else if (shouldApplyAutomaticMappings(resultMap, false)) {
            // 自动映射
            // return createByConstructorSignature(rsw, resultType, constructorArgTypes, constructorArgs);
            throw new UnfinishedFunctionException();
        }
        throw new ExecutorException("Do not know how to create an instance of " + resultType);
    }

    /**
     * 确认{@link #hasTypeHandlerForResultObject(ResultSetWrapper, Class)}为true后调用该方法<br>
     * 获取TypeHandler之后, 由TypeHandler获取Result<br>
     *
     * @return 由TypeHandler获取的结果
     * @see ResultSetWrapper#getTypeHandler(Class, String)
     * @see TypeHandler#getResult(ResultSet, String)
     */
    private Object createPrimitiveResultObject(ResultSetWrapper rsw, ResultMap resultMap, String columnPrefix) throws SQLException {
        final String columnName;
        if (!resultMap.getResultMappings().isEmpty()) {
            final List<ResultMapping> resultMappingList = resultMap.getResultMappings();
            // 为什么是get0
            final ResultMapping mapping = resultMappingList.get(0);
            columnName = DefaultResultSetHandler.prependPrefix(mapping.getColumn(), columnPrefix);
        } else {
            columnName = rsw.getColumnNames().get(0);
        }
        final Class<?> resultType = resultMap.getType();
        final TypeHandler<?> typeHandler = rsw.getTypeHandler(resultType, columnName);
        return typeHandler.getResult(rsw.getResultSet(), columnName);
    }

    /**
     * 如果{@link ResultMap#getAutoMapping()}依据{@link AutoMappingBehavior}判断是否开启AutoMapping
     */
    private boolean shouldApplyAutomaticMappings(ResultMap resultMap, boolean isNested) {
        if (resultMap.getAutoMapping() != null) {
            return resultMap.getAutoMapping();
        }

        if (isNested) {
            return AutoMappingBehavior.FULL == configuration.getAutoMappingBehavior();
        } else {
            return AutoMappingBehavior.NONE != configuration.getAutoMappingBehavior();
        }
    }

    /**
     * 完成自动映射
     */
    private boolean applyAutomaticMappings(
            ResultSetWrapper rsw, ResultMap resultMap,
            MetaObject metaObject, String columnPrefix) throws SQLException {
        List<?> autoMapping = Collections.emptyList();
        // this.createAutomaticMappings(rsw, resultMap, metaObject, columnPrefix);
        boolean foundValues = false;
        if (autoMapping.isEmpty()) {
            return foundValues;
        }
        throw new UnfinishedFunctionException();
        /*
        未完成的AutoMapping, 将其完成
        for (UnMappedColumnAutoMapping mapping : autoMapping) {
            final Object value = mapping.typeHandler.getResult(rsw.getResultSet(), mapping.column);
            if (value != null) {
                foundValues = true;
            }
            if (value != null || (configuration.isCallSettersOnNulls() && !mapping.primitive)) {
                // gcode issue #377, call setter on nulls (value is not 'found')
                metaObject.setValue(mapping.property, value);
            }
        }*/
    }

    /**
     * @param rsw
     * @param resultMap
     * @param metaObject
     * @param lazyLoader   未完成
     * @param columnPrefix
     * @return TODO
     * @throws SQLException
     */
    private boolean applyPropertyMappings(
            ResultSetWrapper rsw, ResultMap resultMap,
            MetaObject metaObject, ResultLoaderMap lazyLoader,
            String columnPrefix)
            throws SQLException {
        // ResultMap中已经把数据库-Java类对应好的字段名
        final List<String> mappedColumnNames = rsw.getMappedColumnNames(resultMap, columnPrefix);
        boolean foundValues = false;
        final List<ResultMapping> fieldMappings = resultMap.getFieldResultMappings();
        for (ResultMapping fieldMapping : fieldMappings) {
            // 遍历每一条resultMapping
            String column = prependPrefix(fieldMapping.getColumn(), columnPrefix);
            if (fieldMapping.getNestedResultMapId() != null) {
                // 使用了嵌套ResultMap, 但又有添加了 column 属性，忽略column
                column = null;
            }
            if (fieldMapping.isCompositeResult() // Composite含有值
                    // or ResultMap中已经把column映射好
                    || (column != null && mappedColumnNames.contains(column.toUpperCase(Locale.ENGLISH)))
                    // or fieldMapping没有配置resultSet
                    || fieldMapping.getResultSet() != null) {
                // 从ResultSet中取值
                Object value = this.getPropertyMappingValue(
                        rsw.getResultSet(), metaObject, fieldMapping, lazyLoader, columnPrefix);
                // 获取java字段
                final String field = fieldMapping.getField();
                if (field == null) {
                    continue;
                }
                if (value == DEFERRED) {
                    // fieldMapping有resultSet?
                    // 包不进的
                    foundValues = true;
                    throw new UnfinishedFunctionException();
                    // continue;
                }
                if (value != null) {
                    foundValues = true;
                }
                if (value != null ||
                        // value==null, 但是:
                        (configuration.isCallSettersOnNulls() // 开启配置
                                &&
                                // 且是引用类型
                                !metaObject.getSetterType(field).isPrimitive())) {
                    // 注入
                    metaObject.setValue(field, value);
                }
            }
        }
        return foundValues;
    }

    private Object getPropertyMappingValue(
            ResultSet rs, MetaObject metaResultObject,
            ResultMapping fieldMapping, ResultLoaderMap lazyLoader,
            String columnPrefix)
            throws SQLException {
        if (fieldMapping.getNestedQueryId() != null) {
            throw new UnfinishedFunctionException();
            // return getNestedQueryMappingValue(rs, metaResultObject,
            // propertyMapping, lazyLoader, columnPrefix);
        }
        if (fieldMapping.getResultSet() != null) {
            throw new UnfinishedFunctionException();
            // addPendingChildRelation(rs, metaResultObject, propertyMapping);
            // TO?DO is that OK?
            // return DEFERRED;
        }
        final TypeHandler<?> typeHandler = fieldMapping.getTypeHandler();
        final String column = prependPrefix(fieldMapping.getColumn(), columnPrefix);
        return typeHandler.getResult(rs, column);
    }

    /**
     * @param parentMapping 若为null, 走{@link #callResultHandler(ResultHandler, DefaultResultContext, Object)}
     * @see #callResultHandler(ResultHandler, DefaultResultContext, Object)
     */
    private void storeObject(
            ResultHandler<?> resultHandler, DefaultResultContext<Object> resultContext,
            Object rowValue,
            ResultMapping parentMapping, ResultSet rs) throws SQLException {
        if (parentMapping != null) {
            throw new UnfinishedFunctionException();
            //linkToParents(rs, parentMapping, rowValue);
        } else {
            this.callResultHandler(resultHandler, resultContext, rowValue);
        }
    }

    /**
     * @param resultHandler {@link ResultHandler#handleResult(ResultContext)}
     * @param resultContext {@link DefaultResultContext#nextResultObject(Object)}
     * @param rowValue      存有数据的实体类
     */
    private void callResultHandler(ResultHandler<?> resultHandler, DefaultResultContext<Object> resultContext, Object rowValue) {
        resultContext.nextResultObject(rowValue);
        ((ResultHandler<Object>) resultHandler).handleResult(resultContext);
    }

    /**
     * 一般来说, 一张表的一个字段对应一个{@link ResultMapping}<br>
     * 一个TypeHandler对应一个字段的类型<br>
     * 如果ResultMap的Type确认存在TypeHandler<br>
     * 以下是返回true的情况:
     * <li>查询语句只查询了一个字段<br>那么这个字段的类型一定是JDBCType,
     * 基本上是会有TypeHandler的, 返回值也容易确认</li>
     * <li>查询语句不只查询了一个字段,returnType却有TypeHandler, 此时是枚举或如int的简单类<br>
     * 喂喂, 返回类型是简单类型, 而不是复合类型, ResultSet返回的字段却有多个吗?大大的不合理啊<br>
     * 明明只需要一个字段就能得出结果, 你却给了我更多的参数, 这选哪个?<br>
     * 这种情况只能忽略ResultSet后面的字段了吧?</li>
     * <li>查询语句不只查询了一个字段,returnType却有TypeHandler, 此时是实体类<br>
     * {@link TypeHandler#getResult(ResultSet, String)}要求返回的是一个完整的类型.<br>
     * 从参数-返回值的关系, 那就是能依靠一个columnName, 就能映射出一整个Entity结果.<br>
     * 从定义上, Entity不适合存在TypeHandler, 但如果只要能实现仅仅依靠一个column就获取整个Entity,<br>
     * (或许自己在TypeHandler里写死了{@link ResultSet#getString(String)}的参数之类的)
     * 那就意味着columnName或许是SQL语句中的第一个第二个不重要<br>
     * 实体类却有TypeHandler</li>
     * <p>
     * 那么就能保证, 若本方法返回的结果为true, 那么
     *
     * @param resultType 从ResultMap中获取的ResultType, {@link ResultMap#getType()}
     * @return ResultSet有唯一的元素或已经存在resultType的TypeHandler
     * (这意味着返回类型是简单类型),
     * 则返回true
     */
    private boolean hasTypeHandlerForResultObject(ResultSetWrapper rsw, Class<?> resultType) {
        if (rsw.getColumnNames().size() == 1) {
            // 只有查询了一个字段
            // 且这个类型一定是简单的类型
            // 确定JdbcType能更快找到指定的typeHandler
            return typeHandlerRegistry.hasTypeHandler(resultType, rsw.getJdbcType(rsw.getColumnNames().get(0)));
        }
        return typeHandlerRegistry.hasTypeHandler(resultType);
    }

    /**
     * 如果{@link Statement}支持多个结果集, 且有下一个ResultSet的话, 返回{@link ResultSetWrapper}<br>
     * 否则返回null
     */
    private ResultSetWrapper getNextResultSet(Statement stmt) {
        // 此方法能够兼容 bad JDBC drivers
        try {
            while (true) {
                if (!stmt.getConnection().getMetaData().supportsMultipleResultSets()) {
                    // 不支持多个结果集
                    break;
                }
                // 来确定是否有更多results
                if (!stmt.getMoreResults()) {
                    // 没有结果集ResultSet了
                    if (stmt.getUpdateCount() == -1) {
                        //  这个语句不是写操作,
                        //  但如果是读操作, 那就应该有ResultSet
                        //  但没有, 那就是没有语句了
                        //  那就是真没有MoreResultSet了
                        break;
                    } else {
                        // 这个语句是写操作, 这个语句上没有ResultSet了
                        // 但是下一个语句可能还有ResultSet
                        continue;
                    }
                }
                ResultSet rs = stmt.getResultSet();
                if (rs != null) {
                    // 循环出口
                    return new ResultSetWrapper(rs, configuration);
                }
                // rs==null, 继续寻找下一个
                // 这种一般是写操作
            }
        } catch (Exception ignored) {
        }
        return null;
    }


    /**
     * TODO
     */
    @Override
    public <E> Cursor<E> handleCursorResultSets(Statement stmt) throws SQLException {
        throw new UnfinishedFunctionException();
    }

    /**
     * TODO
     */
    @Override
    public void handleOutputParameters(CallableStatement cs) throws SQLException {
        throw new UnfinishedFunctionException();
    }

    private void cleanUpAfterHandlingResultSet() {
        nestedResultObjects.clear();
    }

    private static void closeResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            // ignore
        }
    }

    /**
     * <pre>{@code
     *  !context.isStopped() &&
     *  context.getResultCount() < rowBounds.getLimit();
     * }</pre>
     *
     * @param context   {@link ResultContext#isStopped()}, {@link ResultContext#getResultCount()}
     * @param rowBounds {@link RowBounds#getLimit()}
     * @return 还可以处理更多行则返回true
     * @see ResultContext
     * @see RowBounds
     */
    private static boolean shouldProcessMoreRows(ResultContext<?> context, RowBounds rowBounds) {
        return !context.isStopped() && context.getResultCount() < rowBounds.getLimit();
    }

    private static String prependPrefix(String columnName, String prefix) {
        if (columnName == null || columnName.isEmpty() || prefix == null || prefix.isEmpty()) {
            return columnName;
        }
        return prefix + columnName;
    }
}
