package org.harvey.batis.mapping;

import lombok.Getter;
import org.harvey.batis.config.Configuration;
import org.harvey.batis.util.ArrayUtil;
import org.harvey.batis.util.enums.JdbcType;
import org.harvey.batis.util.type.TypeHandler;
import org.harvey.batis.util.type.TypeHandlerRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-04 16:17
 */
public class ResultMapping {

    private Configuration configuration;
    /**
     * java field
     */
    @Getter
    private String field;
    /**
     * 数据库column
     */
    @Getter
    private String column;
    private Class<?> javaType;
    private JdbcType jdbcType;

    @Getter
    private TypeHandler<?> typeHandler;
    @Getter
    private String nestedResultMapId;
    @Getter
    private String nestedQueryId;

    private Set<String> notNullColumns;
    private String columnPrefix;
    // private List<ResultFlag> flags;
    /**
     * TODO 复合
     */
    private List<ResultMapping> composites;
    @Getter
    private String resultSet;
    private String foreignColumn;
    @Getter
    private boolean lazy;

    private ResultMapping() {
        // 只能由Builder管理
    }

    public boolean isCompositeResult() {
        return this.composites != null && !this.composites.isEmpty();
    }


    public static class Builder {
        private final ResultMapping resultMapping = new ResultMapping();

        public Builder(Configuration configuration, String field, String column, TypeHandler<?> typeHandler) {
            this(configuration, field);
            resultMapping.column = column;
            resultMapping.typeHandler = typeHandler;
        }

        public Builder(Configuration configuration, String field, String column, Class<?> javaType) {
            this(configuration, field);
            resultMapping.column = column;
            resultMapping.javaType = javaType;
        }

        public Builder(Configuration configuration, String field) {
            resultMapping.configuration = configuration;
            resultMapping.field = field;
            // resultMapping.flags = new ArrayList<>();
            resultMapping.composites = new ArrayList<>();
            resultMapping.lazy = configuration.isLazyLoadingEnabled();
        }

        public Builder column(String column) {
            resultMapping.column = column;
            return this;
        }

        public Builder javaType(Class<?> javaType) {
            resultMapping.javaType = javaType;
            return this;
        }

        public Builder jdbcType(JdbcType jdbcType) {
            resultMapping.jdbcType = jdbcType;
            return this;
        }

        public Builder nestedResultMapId(String nestedResultMapId) {
            resultMapping.nestedResultMapId = nestedResultMapId;
            return this;
        }

        public Builder nestedQueryId(String nestedQueryId) {
            resultMapping.nestedQueryId = nestedQueryId;
            return this;
        }

        public Builder resultSet(String resultSet) {
            resultMapping.resultSet = resultSet;
            return this;
        }

        public Builder foreignColumn(String foreignColumn) {
            resultMapping.foreignColumn = foreignColumn;
            return this;
        }

        public Builder notNullColumns(Set<String> notNullColumns) {
            resultMapping.notNullColumns = notNullColumns;
            return this;
        }

        public Builder columnPrefix(String columnPrefix) {
            resultMapping.columnPrefix = columnPrefix;
            return this;
        }

        /*public Builder flags(List<ResultFlag> flags) {
            resultMapping.flags = flags;
            return this;
        }*/

        public Builder typeHandler(TypeHandler<?> typeHandler) {
            resultMapping.typeHandler = typeHandler;
            return this;
        }

        public Builder composites(List<ResultMapping> composites) {
            resultMapping.composites = composites;
            return this;
        }

        public Builder lazy(boolean lazy) {
            resultMapping.lazy = lazy;
            return this;
        }

        public ResultMapping build() {
            // lock down collections
            // resultMapping.flags = Collections.unmodifiableList(resultMapping.flags);
            resultMapping.composites = Collections.unmodifiableList(resultMapping.composites);
            resolveTypeHandler();
            validate();
            return resultMapping;
        }

        private void validate() {
            // Issue #697: cannot define both nestedQueryId and nestedResultMapId
            if (resultMapping.nestedQueryId != null &&
                    resultMapping.nestedResultMapId != null) {
                throw new IllegalStateException("Cannot define both nestedQueryId and nestedResultMapId in property " + resultMapping.field);
            }
            // Issue #5: there should be no mappings without type handler
            if (resultMapping.nestedQueryId == null &&
                    resultMapping.nestedResultMapId == null &&
                    resultMapping.typeHandler == null) {
                throw new IllegalStateException("No type handler found for property " + resultMapping.field);
            }
            // Issue #4 and GH #39: column is optional only in nested result maps but not in the rest
            if (resultMapping.nestedResultMapId == null &&
                    resultMapping.column == null &&
                    resultMapping.composites.isEmpty()) {
                throw new IllegalStateException("Mapping is missing column attribute for property " + resultMapping.field);
            }
            if (resultMapping.resultSet == null) {
                return;
            }
            int numColumns = 0;
            if (resultMapping.column != null) {
                numColumns = ArrayUtil.splitCount(resultMapping.column, ',');
            }
            int numForeignColumns = 0;
            if (resultMapping.foreignColumn != null) {
                numForeignColumns = ArrayUtil.splitCount(resultMapping.foreignColumn, ',');
            }
            if (numColumns != numForeignColumns) {
                throw new IllegalStateException("There should be the same number of columns and foreignColumns in property " + resultMapping.field);
            }
        }

        /**
         * 如果最后要Build了还是没有TypeHandler, 就要从{@link TypeHandlerRegistry}中取了
         */
        private void resolveTypeHandler() {
            if (resultMapping.typeHandler == null && resultMapping.javaType != null) {
                Configuration configuration = resultMapping.configuration;
                TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
                resultMapping.typeHandler = typeHandlerRegistry.getTypeHandler(resultMapping.javaType, resultMapping.jdbcType);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ResultMapping that = (ResultMapping) o;

        return field != null && field.equals(that.field);
    }

    @Override
    public int hashCode() {
        if (field != null) {
            return field.hashCode();
        }
        if (column != null) {
            return column.hashCode();
        }
        return 0;
    }

    @Override
    public String toString() {

        return "ResultMapping{" + "property='" + field + '\'' +
                ", column='" + column + '\'' +
                ", javaType=" + javaType +
                ", jdbcType=" + jdbcType +
                ", nestedResultMapId='" + nestedResultMapId + '\'' +
                // ", flags=" + flags +
                ", nestedQueryId='" + nestedQueryId + '\'' +
                ", notNullColumns=" + notNullColumns +
                ", columnPrefix='" + columnPrefix + '\'' +
                ", composites=" + composites +
                ", resultSet='" + resultSet + '\'' +
                ", foreignColumn='" + foreignColumn + '\'' +
                ", lazy=" + lazy +
                '}';
    }
}
