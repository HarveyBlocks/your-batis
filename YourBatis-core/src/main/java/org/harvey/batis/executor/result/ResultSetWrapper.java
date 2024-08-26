package org.harvey.batis.executor.result;

import lombok.Getter;
import org.harvey.batis.config.Configuration;
import org.harvey.batis.io.Resources;
import org.harvey.batis.mapping.ResultMap;
import org.harvey.batis.util.enums.JdbcType;
import org.harvey.batis.util.type.ObjectTypeHandler;
import org.harvey.batis.util.type.TypeHandler;
import org.harvey.batis.util.type.TypeHandlerRegistry;
import org.harvey.batis.util.type.UnknownTypeHandler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * {@link ResultSet}的包装类, {@link ResultSet}的Column到{@link ResultMap}的Column和Flied映射的转变
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-21 16:21
 */
public class ResultSetWrapper {
    private final TypeHandlerRegistry typeHandlerRegistry;
    @Getter
    private final ResultSet resultSet;
    /**
     * @see Configuration#isUseColumnLabel()
     * @see ResultSetMetaData#getColumnLabel(int)
     * @see ResultSetMetaData#getColumnName(int)
     */
    @Getter
    private final List<String> columnNames = new ArrayList<>();
    /**
     * @see ResultSetMetaData#getColumnClassName(int)
     */
    private final List<String> classNames = new ArrayList<>();
    private final List<JdbcType> jdbcTypes = new ArrayList<>();
    private final Map<String, Map<Class<?>, TypeHandler<?>>> typeHandlerMap = new HashMap<>();
    /**
     * @see #loadMappedAndUnmappedColumnNames(ResultMap, String)
     * @see #getMapKey(ResultMap, String) 键
     * @see #classNames classNames的一部分为值
     */
    private final Map<String, List<String>> mappedColumnNamesMap = new HashMap<>();
    /**
     * @see #loadMappedAndUnmappedColumnNames(ResultMap, String)
     * @see #getMapKey(ResultMap, String) 键
     * @see #classNames classNames的一部分为值
     */
    private final Map<String, List<String>> unMappedColumnNamesMap = new HashMap<>();

    public ResultSetWrapper(ResultSet rs, Configuration configuration) throws SQLException {
        super();
        this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        this.resultSet = rs;
        final ResultSetMetaData metaData = rs.getMetaData();
        final int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            // 遍历metaData, 以获取其各种信息
            columnNames.add(configuration.isUseColumnLabel() ? metaData.getColumnLabel(i) : metaData.getColumnName(i));
            jdbcTypes.add(JdbcType.forCode(metaData.getColumnType(i))); // 获取JDBCType
            classNames.add(metaData.getColumnClassName(i)); // 获取JavaClass
        }
    }

    /**
     * 遍历{@link #columnNames}从{@link #jdbcTypes}中获取目标
     */
    public JdbcType getJdbcType(String columnName) {
        for (int i = 0; i < columnNames.size(); i++) {
            if (columnNames.get(i).equalsIgnoreCase(columnName)) {
                return jdbcTypes.get(i);
            }
        }
        return null;
    }


    /**
     * @param columnName   从{@link #typeHandlerMap}中获取{@link Map}: columnHandlers, Class: TypeHandler
     * @param propertyType 从columnHandlers中获取{@link TypeHandler}, 并返回;<br>
     *                     如果不存在, 则从{@link #typeHandlerRegistry}中取出TypeHandler, 并保存columnHandlers中
     * @return 如果实在没有, 则返回{@link ObjectTypeHandler}, 而不是{@link UnknownTypeHandler}
     */
    public TypeHandler<?> getTypeHandler(Class<?> propertyType, String columnName) {
        // 从typeHandlerMap获取typeHandlers
        Map<Class<?>, TypeHandler<?>> columnHandlers = typeHandlerMap.computeIfAbsent(columnName, key -> {
            HashMap<Class<?>, TypeHandler<?>> newValue = new HashMap<>();
            typeHandlerMap.put(key, newValue);
            return newValue;
        });
        // 第一次从columnHandlers中获取
        TypeHandler<?> handler = columnHandlers.get(propertyType);
        if (handler != null) {
            return handler;
        }
        // 获取
        JdbcType jdbcType = this.getJdbcType(columnName);
        handler = typeHandlerRegistry.getTypeHandler(propertyType, jdbcType);
        if (handler != null && !(handler instanceof UnknownTypeHandler)) {
            return columnHandlers.put(propertyType, handler);
        }
        final int index = columnNames.indexOf(columnName);
        final Class<?> javaType = this.resolveClass(classNames.get(index));
        if (javaType != null && jdbcType != null) {
            handler = typeHandlerRegistry.getTypeHandler(javaType, jdbcType);
        } else if (javaType != null) {
            handler = typeHandlerRegistry.getTypeHandler(javaType);
        } else if (jdbcType != null) {
            handler = typeHandlerRegistry.getTypeHandler(jdbcType);
        }
        if (handler == null || handler instanceof UnknownTypeHandler) {
            handler = new ObjectTypeHandler();
        }
        return columnHandlers.put(propertyType, handler);
    }

    /**
     * @see Resources#classForName(String)
     */
    private Class<?> resolveClass(String className) {
        try {
            if (className != null) {
                return Resources.classForName(className);
            }
        } catch (ClassNotFoundException ignore) {
        }
        return null;
    }

    /**
     * @param resultMap Java和JDBC的映射关系
     * @return {@link ResultMap}中已经把数据库-Java类对应好的字段名
     * @see #loadMappedAndUnmappedColumnNames(ResultMap, String)
     * @see #mappedColumnNamesMap
     */
    public List<String> getMappedColumnNames(ResultMap resultMap, String columnPrefix) throws SQLException {
        String mapKey = ResultSetWrapper.getMapKey(resultMap, columnPrefix);
        List<String> mappedColumnNames = mappedColumnNamesMap.get(mapKey);
        if (mappedColumnNames == null) {
            this.loadMappedAndUnmappedColumnNames(resultMap, columnPrefix);
            mappedColumnNames = mappedColumnNamesMap.get(mapKey);
        }
        return mappedColumnNames;
    }

    /**
     * 加载{@link #mappedColumnNamesMap}和{@link #unMappedColumnNamesMap}的列名称
     *
     * @param columnPrefix 前缀+{@link #columnNames}
     * @see #prependPrefixes(Set, String)
     * @see #columnNames
     */
    private void loadMappedAndUnmappedColumnNames(ResultMap resultMap, String columnPrefix) {
        // 结果元素
        List<String> mappedColumnNames = new ArrayList<>();
        List<String> unmappedColumnNames = new ArrayList<>();
        // columnPrefix转大写
        final String upperColumnPrefix = columnPrefix == null ? null : columnPrefix.toUpperCase(Locale.ENGLISH);
        // 所有元素加前缀
        final Set<String> mappedColumns = ResultSetWrapper.prependPrefixes(resultMap.getMappedColumns(), upperColumnPrefix);
        for (String columnName : columnNames) {
            // columnName转大写
            final String upperColumnName = columnName.toUpperCase(Locale.ENGLISH);
            // 检查是否在mappedColumns中, 存在表示完成, 不存在表示未完成
            if (mappedColumns.contains(upperColumnName)) {
                // 完成
                mappedColumnNames.add(upperColumnName);
            } else {
                // 为完成
                unmappedColumnNames.add(columnName);
            }
        }
        mappedColumnNamesMap.put(getMapKey(resultMap, columnPrefix), mappedColumnNames);
        unMappedColumnNamesMap.put(getMapKey(resultMap, columnPrefix), unmappedColumnNames);
    }

    /**
     * @param columnNames {@link ResultMap#getMappedColumns()}
     * @return 所有columnNames的元素加上prefix之后返回
     */
    private static Set<String> prependPrefixes(Set<String> columnNames, String prefix) {
        if (columnNames == null || columnNames.isEmpty() || prefix == null || prefix.isEmpty()) {
            return columnNames;
        }
        final Set<String> prefixed = new HashSet<>();
        for (String columnName : columnNames) {
            prefixed.add(prefix + columnName);
        }
        return prefixed;
    }

    /**
     * <pre>{@code
     *  resultMap.getId() + ":" + columnPrefix
     * }</pre>
     *
     * @return 有了columnPrefix, 从{@link #mappedColumnNamesMap}的键
     */
    public static String getMapKey(ResultMap resultMap, String columnPrefix) {
        return resultMap.getId() + ":" + columnPrefix;
    }
}
