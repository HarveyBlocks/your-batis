package org.harvey.batis.builder;

import lombok.Getter;
import org.harvey.batis.cache.Cache;
import org.harvey.batis.config.Configuration;
import org.harvey.batis.exception.UnfinishedFunctionException;
import org.harvey.batis.exception.builder.BuilderException;
import org.harvey.batis.exception.builder.IncompleteElementException;
import org.harvey.batis.executor.key.generator.KeyGenerator;
import org.harvey.batis.mapping.*;
import org.harvey.batis.mapping.sqlsource.SqlSource;
import org.harvey.batis.reflection.MetaClass;
import org.harvey.batis.scripting.LanguageDriver;
import org.harvey.batis.util.ArrayUtil;
import org.harvey.batis.util.ErrorContext;
import org.harvey.batis.util.enums.SqlCommandType;
import org.harvey.batis.util.enums.StatementType;
import org.harvey.batis.util.type.TypeHandler;

import java.util.*;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-04 16:04
 */
public class MapperBuilderAssistant extends BaseBuilder {
    private final String resource;
    /**
     * MapperXml的TargetMapper接口的全类名
     */
    @Getter
    private String currentTargetMapper;

    /**
     * {@inheritDoc}
     * TODO
     */
    public MapperBuilderAssistant(Configuration configuration, String resource) {
        super(configuration);
        ErrorContext.instance().setResource(resource);
        this.resource = resource;
    }

    /**
     * TODO
     */
    public Cache useCacheRef(String namespace) {
        throw new UnfinishedFunctionException();
    }

    /**
     * TODO
     *
     * @param type           实体类型
     * @param id             resultMap的id
     * @param resultMappings resultMap的列-字段的映射关系
     * @throws IncompleteElementException
     */
    public ResultMap addResultMap(String id, Class<?> type, List<ResultMapping> resultMappings) {
        id = this.applyCurrentNamespace(id, false);
        ResultMap resultMap = new ResultMap.Builder(configuration, id, type, resultMappings, null).build();
        configuration.addResultMap(resultMap);
        return resultMap;
    }

    /**
     * 通过加前缀的方式 为这个base指定其对应的{@link #currentTargetMapper}
     *
     * @param base        需要被加上这个MapperTarget标识的一些字符串<br>
     *                    base=resultMapId/methodName/...
     * @param isReference 这个base是不是从其他Mapper映射文件来的?<br>
     *                    如果是的话(isReference=true)<br>
     *                    且已经有其他currentTargetMapper加持<br>
     *                    那么不会加上本Mapper的{@link #currentTargetMapper}<br>
     *                    否则会加上本Mapper的{@link #currentTargetMapper}<br>
     *                    如果是的话(isReference=false)<br>
     *                    且已经有其他currentTargetMapper加持, 抛出异常<br>
     *                    如果有自己currentTargetMapper加持, 直接返回<br>
     *                    否则会加上本Mapper的{@link #currentTargetMapper}
     * @return {@link #currentTargetMapper}+"."+base(如果需要的话), <br>
     * 对于target=Mapper.class.getName(); base=methodName的情况下,<br>
     * 这就是StatementKey<br>
     * {@code type.getName() + "." + method.getName();}<br>
     */
    public String applyCurrentNamespace(String base, boolean isReference) {
        if (base == null) {
            return null;
        }
        if (isReference) {
            // 是不是有其他currentTargetMapper加持?
            if (base.contains(".")) {
                return base;
            }
            // 没有? 那直接返回
        } else {
            // 这个ID是否已经有currentTargetMapper加持
            if (base.startsWith(currentTargetMapper + ".")) {
                return base;
            }
            // 没有?
            // 是不是有其他currentTargetMapper加持?
            if (base.contains(".")) {
                // 这不正常
                throw new BuilderException("Dots are not allowed in element names, please remove it from " + base);
            }
        }
        return currentTargetMapper + "." + base;
    }

    /**
     * 大抵是用来检查的TargetMapper吧?<br>
     * XML文件里会指定自己的目标Mapper接口<br>
     * 要求字段{@link #currentTargetMapper}为null时才能注入currentNamespace<br>
     * 如果不为null, currentNamespace一定要和currentNamespace一样, 否则抛异常
     *
     * @param currentTargetMapper 不能为null
     */
    public void setCurrentTargetMapper(String currentTargetMapper) {
        if (currentTargetMapper == null) {
            throw new BuilderException("The mapper element requires a namespace attribute to be specified.");
        }
        if (this.currentTargetMapper == null) {
            this.currentTargetMapper = currentTargetMapper;
            return;
        }
        if (this.currentTargetMapper.equals(currentTargetMapper)) {
            return;
        }
        throw new BuilderException("Wrong namespace. Expected '" + this.currentTargetMapper + "' but found '" + currentTargetMapper + "'.");


    }

    /**
     * 构建ResultMapping
     *
     * @param entityClass field所在的Java实体类, ResultMap的Attribution
     * @param field       Java实体类中的字段, 与Column对应
     * @param column      SQL数据库中的Column
     * @see ResultMapping.Builder
     */
    public ResultMapping buildResultMapping(
            Class<?> entityClass, String field, String column, Class<? extends TypeHandler<?>> ignore) {
        return buildResultMapping(entityClass, field, column, ignore, null, null);
    }

    /**
     * 构建ResultMapping
     *
     * @param entityClass field所在的Java实体类, ResultMap的Attribution
     * @param field       Java实体类中的字段, 与Column对应
     * @param column      SQL数据库中的Column
     * @see ResultMapping.Builder
     */
    public ResultMapping buildResultMapping(
            Class<?> entityClass, String field, String column,
            Class<? extends TypeHandler<?>> ignore,
            String nestedSelect, String foreignColumn) {
        Class<?> javaTypeClass = resolveResultJavaType(entityClass, field, /*javaType*/null);
        // TypeHandler<?> typeHandlerInstance = resolveTypeHandler(javaTypeClass, typeHandler);
        // List<ResultMapping> composites= Collections.emptyList();
        // composites = parseCompositeColumnName(column);
        List<ResultMapping> composites;
        if ((nestedSelect == null || nestedSelect.isEmpty()) && (foreignColumn == null || foreignColumn.isEmpty())) {
            composites = Collections.emptyList();
        } else {
            throw new UnfinishedFunctionException();
            // composites = parseCompositeColumnName(column);
        }
        return new ResultMapping.Builder(configuration, field, column, javaTypeClass)
                .jdbcType(null)
                .nestedQueryId(applyCurrentNamespace(null, true))
                .nestedResultMapId(applyCurrentNamespace(null, true))
                .resultSet(null)
                .typeHandler(null)
                // .flags(flags == null ? new ArrayList<>() : flags)
                .composites(composites)
                .notNullColumns(parseMultipleColumnNames(null))
                .columnPrefix(null)
                .foreignColumn(null)
                .lazy(false)
                .build();
    }

    /**
     * 从columnName中依据{@code ['{', '}', ',', ' ']}作为分隔符, 筛选出多个column
     *
     * @return column的集合
     */
    private Set<String> parseMultipleColumnNames(String columnName) {
        Set<String> columns = new HashSet<>();
        if (columnName == null) {
            return columns;
        }
        if (columnName.indexOf(',') == -1) {
            columns.add(columnName);
            return columns;
        }
        StringTokenizer parser = new StringTokenizer(columnName, "{}, ", false);
        while (parser.hasMoreTokens()) {
            String column = parser.nextToken();
            columns.add(column);
        }

        return columns;
    }

    /**
     * 从entityClass中获取field的类型
     *
     * @param entityClass Java的实体类型, 内含field
     * @param field       字段field, 其形如{@code "school.student[12].math"}
     * @param javaType    field的类型, 如果不为null, 直接返回该值, 否则用反射从entityClass获取
     * @return 如果javaType不为null, 直接返回该值, 否则用反射从entityClass获取field的类型
     */
    private Class<?> resolveResultJavaType(Class<?> entityClass, String field, Class<?> javaType) {
        if (javaType != null) {
            return javaType;
        }
        if (field == null) {
            return Object.class;
        }
        try {
            MetaClass metaResultType = MetaClass.forClass(entityClass, configuration.getReflectorFactory());
            Class<?> fieldType = metaResultType.getSetterType(field);
            return fieldType == null ? Object.class : fieldType;
        } catch (Exception e) {
            // ignore, following null check statement will deal with the situation
        }
        return Object.class;
    }

    public MappedStatement addMappedStatement(String methodName, SqlSource sqlSource, SqlCommandType sqlCommandType,
                                              String resultMap, Class<?> resultType, LanguageDriver lang, KeyGenerator keyGenerator) {
        String statementId = this.applyCurrentNamespace(methodName, false);
        MappedStatement.Builder statementBuilder = new MappedStatement
                .Builder(configuration, statementId, sqlSource, sqlCommandType)
                .resource(resource)
                .fetchSize(null)
                .timeout(null)
                .statementType(StatementType.PREPARED)
                .keyGenerator(keyGenerator)
                //.keyProperty(keyProperty)
                .keyColumn(null)
                //.databaseId(databaseId)
                .lang(lang)
                //.resultOrdered(resultOrdered)
                .resultSets(null)
                .resultMaps(getStatementResultMaps(resultMap, resultType, statementId))
                .resultSetType(null)
                .flushCacheRequired(false/*valueOrDefault(flushCache, !isSelect)*/)
                //.useCache(valueOrDefault(useCache, isSelect))
                //.cache(currentCache)
                ;
        ParameterMap statementParameterMap = getStatementParameterMap(null, null, statementId);
        if (statementParameterMap != null) {
            statementBuilder.parameterMap(statementParameterMap);
        }
        MappedStatement statement = statementBuilder.build();
        return configuration.addMappedStatement(statement);
    }

    private ParameterMap getStatementParameterMap(
            String parameterMapName, Class<?> parameterTypeClass, String statementId) {
        parameterMapName = applyCurrentNamespace(parameterMapName, true);
        ParameterMap parameterMap = null;
        if (parameterMapName == null && parameterTypeClass == null) {
            return parameterMap;
        }
        if (parameterMapName != null) {
            try {
                return configuration.getParameterMap(parameterMapName);
            } catch (IllegalArgumentException e) {
                throw new IncompleteElementException("Could not find parameter map " + parameterMapName, e);
            }

        }
        List<ParameterMapping> parameterMappings = new ArrayList<>();
        return new ParameterMap.Builder(
                statementId + "-Inline",
                parameterTypeClass,
                parameterMappings).build();

    }

    /**
     * @param resultMap 以","分割的多个resultMap
     */
    private List<ResultMap> getStatementResultMaps(String resultMap, Class<?> resultType, String statementId) {
        // 获取resultMap的全限定名, 以获取具体的resultMap
        // 如果resultMap参数为null, 则返回null
        // isReference=true
        // 也就是说, 通过在mapper.xml的各sql的resultMap="?"里设置"target.result-map"的方式
        // 来调用其他的mapper.xml的配置
        List<ResultMap> resultMaps = new ArrayList<>();
        if (resultMap == null && resultType == null) {
            return resultMaps;
        }
        if (resultMap != null) {
            String[] resultMapNames = ArrayUtil.split(resultMap, ResultMap.ATTRIBUTION_SEPARATOR);
            for (String resultMapName : resultMapNames) {
                try {
                    resultMaps.add(configuration.getResultMap(resultMapName.trim()));
                } catch (IllegalArgumentException e) {
                    throw new IncompleteElementException("Could not find result map '" + resultMapName + "' referenced from '" + statementId + "'", e);
                }
            }
            return resultMaps;
        }
        // 如果resultMap为null, resultType不为null, 则采用resultType
        ResultMap inlineResultMap = new ResultMap.Builder(
                configuration,
                statementId + "-Inline",
                resultType,
                new ArrayList<>(),
                null).build();
        resultMaps.add(inlineResultMap);
        return resultMaps;
    }


}
