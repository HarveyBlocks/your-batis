package org.harvey.batis.mapping;

import lombok.Getter;
import org.harvey.batis.config.Configuration;
import org.harvey.batis.io.log.Log;
import org.harvey.batis.io.log.LogFactory;
import org.harvey.batis.parsing.MapperXmlConstants;

import java.util.*;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-04 16:18
 */
public class ResultMap {

    public static final String USE_RESULT_TYPE_ID_SUFFIX = "-Inline";
    public static final char ATTRIBUTION_SEPARATOR = MapperXmlConstants.Sql.RESULT_MAP_ATTRIBUTION_SEPARATOR;
    private Configuration configuration;

    @Getter
    private String id;
    @Getter
    private Class<?> type;
    @Getter
    private List<ResultMapping> resultMappings;
    /**
     * TODO  不知与resultMappings有何区别
     */
    private List<ResultMapping> idResultMappings;
    /**
     * TODO  不知与resultMappings有何区别
     */
    @Getter
    private List<ResultMapping> fieldResultMappings;
    /**
     * 所有的ResultMapping的Column的信息, 全大写
     *
     * @see ResultMapping#getColumn()
     */
    @Getter
    private Set<String> mappedColumns;
    /**
     * 所有的ResultMapping的Field的信息
     *
     * @see ResultMapping#getField()
     */
    private Set<String> mappedFields;
    @Deprecated
    @Getter
    private final boolean hasNestedResultMaps = false;
    @Deprecated
    private final boolean hasNestedQueries = false;
    @Getter
    private Boolean autoMapping;

    /**
     * 一个{@link ResultMap}, 对应一个{@link #type}, <br>
     * 此{@link #type}可为Entity, 可以配置构造器来实例化这个Entity<br>
     * 如果不配置实例化{@link #type}对应Entity构造器, 那就使用默认无参构造器
     */
    @Getter
    private final List<ResultMapping> constructorResultMappings = Collections.unmodifiableList(new ArrayList<>());
    /* private List<ResultMapping> propertyResultMappings;
    private Set<String> mappedProperties;*/

    private ResultMap() {
        // 只有本类的Builder才能实例化ResultMap
    }

    public static class Builder {
        private static final Log LOG = LogFactory.getLog(Builder.class);

        private final ResultMap resultMap = new ResultMap();

        /**
         * @param id          形如{@code target.result-map}, 但是如果result-map为null儿resultType不为null,
         *                    会在末尾加上{@code "-Inline"}
         * @param autoMapping Unknown
         */
        public Builder(Configuration configuration, String id, Class<?> type,
                       List<ResultMapping> resultMappings, Boolean autoMapping) {
            resultMap.configuration = configuration;
            resultMap.id = id;
            resultMap.type = type;
            resultMap.resultMappings = resultMappings;
            resultMap.autoMapping = autoMapping;
        }

        public ResultMap build() {
            if (resultMap.id == null) {
                throw new IllegalArgumentException("ResultMaps must have an id");
            }
            resultMap.mappedColumns = new HashSet<>();
            resultMap.mappedFields = new HashSet<>();
            resultMap.idResultMappings = new ArrayList<>();
            resultMap.fieldResultMappings = new ArrayList<>();
            for (ResultMapping resultMapping : resultMap.resultMappings) {
                final String column = resultMapping.getColumn();
                if (column != null) {
                    resultMap.mappedColumns.add(column.toUpperCase(Locale.ENGLISH));
                }
                final String field = resultMapping.getField();
                if (field != null) {
                    resultMap.mappedFields.add(field);
                }
                resultMap.fieldResultMappings.add(resultMapping);
            }
            if (resultMap.idResultMappings.isEmpty()) {
                resultMap.idResultMappings.addAll(resultMap.resultMappings);
            }
            // 锁定集合, 这些集合不能写只能读了
            resultMap.resultMappings = Collections.unmodifiableList(resultMap.resultMappings);
            resultMap.idResultMappings = Collections.unmodifiableList(resultMap.idResultMappings);
            resultMap.fieldResultMappings = Collections.unmodifiableList(resultMap.fieldResultMappings);
            resultMap.mappedColumns = Collections.unmodifiableSet(resultMap.mappedColumns);
            return resultMap;
        }
    }
}
