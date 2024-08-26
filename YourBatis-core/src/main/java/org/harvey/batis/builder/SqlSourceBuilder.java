package org.harvey.batis.builder;

import lombok.Getter;
import org.harvey.batis.config.Configuration;
import org.harvey.batis.exception.UnfinishedFunctionException;
import org.harvey.batis.exception.builder.BuilderException;
import org.harvey.batis.mapping.ParameterMapping;
import org.harvey.batis.mapping.sqlsource.SqlSource;
import org.harvey.batis.mapping.sqlsource.StaticSqlSource;
import org.harvey.batis.parsing.GenericTokenParser;
import org.harvey.batis.parsing.TokenHandler;
import org.harvey.batis.reflection.MetaClass;
import org.harvey.batis.reflection.MetaObject;

import java.util.*;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-13 17:01
 */
public class SqlSourceBuilder extends BaseBuilder {
    /**
     * 可选可注入的配置值
     */
    private static final String[] PARAMETER_PROPERTIES = new String[]{
            "javaType", "jdbcType", "mode", "numericScale", "resultMap", "typeHandler", "jdbcTypeName"};
    public static final String CLOSE_TOKEN = "}";
    public static final String PARAMETER_OPEN_TOKEN = "#{";
    public static final String PARAMETER_CLOSE_TOKEN = CLOSE_TOKEN;
    public static final String SCRIPT_OPEN_TOKEN = "${";
    public static final String SCRIPT_CLOSE_TOKEN = CLOSE_TOKEN;
    public SqlSourceBuilder(Configuration configuration) {
        super(configuration);
    }

    /**
     * 纯文本SQL
     */
    public SqlSource build(String originalSql, Class<?> parameterType, Map<String, Object> additionalParameters) {
        ParameterMappingTokenHandler handler = new ParameterMappingTokenHandler(configuration, parameterType, additionalParameters);
        GenericTokenParser parser = new GenericTokenParser(PARAMETER_OPEN_TOKEN, PARAMETER_CLOSE_TOKEN, handler);
        String sql = parser.parse(
                configuration.isShrinkWhitespacesInSql() ?
                        removeExtraWhitespaces(originalSql) :
                        originalSql);
        return new StaticSqlSource(configuration, sql, handler.getParameterMappings());
    }

    /**
     * 用一个{@code " "}去顶替多余的连续的空白符
     */
    public static String removeExtraWhitespaces(String original) {
        StringTokenizer tokenizer = new StringTokenizer(original);
        StringJoiner joiner = new StringJoiner(" ");
        while (tokenizer.hasMoreTokens()) {
            joiner.add(tokenizer.nextToken());
        }
        return joiner.toString();
    }


    private static class ParameterMappingTokenHandler extends BaseBuilder implements TokenHandler {
        /**
         * SQL语句中, 每一个?对应的Parameter(按顺序)
         */
        @Getter
        private final List<ParameterMapping> parameterMappings = new ArrayList<>();
        private final Class<?> parameterType;
        /**
         * @see org.harvey.batis.reflection.wrapper.MapWrapper
         */
        private final MetaObject metaParameters;
        public static final String SQL_PARAMETER_PLACEHOLDER = "?";

        /**
         * @param parameterType        未实现, 一般都是Object
         * @param additionalParameters 目前一般都是new HashMap()
         */
        public ParameterMappingTokenHandler(Configuration configuration, Class<?> parameterType, Map<String, Object> additionalParameters) {
            super(configuration);
            this.parameterType = parameterType;
            this.metaParameters = configuration.newMetaObject(additionalParameters);
        }

        @Override
        public String handleToken(String content) {
            parameterMappings.add(buildParameterMapping(content));
            return SQL_PARAMETER_PLACEHOLDER;
        }

        private ParameterMapping buildParameterMapping(String content) {
            Map<String, String> propertiesMap = parseParameterMapping(content);
            String property = propertiesMap.get("property");
            Class<?> propertyType; // content配置类型
            if (metaParameters.hasGetter(property)) {
                // 从metaParameters(Map)里获取content配置类型
                propertyType = metaParameters.getGetterType(property);
            } else if (property == null || Map.class.isAssignableFrom(parameterType)) {
                // content为null, 或parameterType是Map的子类
                propertyType = Object.class;
            } else {
                // 用parameterType实例化MetaClass, 以此获取字段
                MetaClass metaClass = MetaClass.forClass(parameterType, configuration.getReflectorFactory());
                if (metaClass.hasGetter(property)) {
                    propertyType = metaClass.getGetterType(property);
                } else {
                    // 一般最终都是这个分支
                    propertyType = Object.class;
                }
            }
            ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, property, propertyType);
            Class<?> javaType = propertyType;
            String typeHandlerAlias = null;
            for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue();
                if ("javaType".equals(name)) {
                    javaType = resolveClass(value);
                    builder.javaType(javaType);
                    throw new UnfinishedFunctionException(javaType);
                } else if ("jdbcType".equals(name)) {
                    // builder.jdbcType(resolveJdbcType(value));
                    throw new UnfinishedFunctionException(value);
                } else if ("mode".equals(name)) {
                    builder.mode(super.resolveParameterMode(value));
                } else if ("numericScale".equals(name)) {
                    // builder.numericScale(Integer.valueOf(value));
                    throw new UnfinishedFunctionException();
                } else if ("resultMap".equals(name)) {
                    builder.resultMapId(value);
                } else if ("typeHandler".equals(name)) {
                    typeHandlerAlias = value;
                } else if ("jdbcTypeName".equals(name)) {
                    // builder.jdbcTypeName(value);
                    throw new UnfinishedFunctionException();
                } else if ("property".equals(name)) {
                    // Do Nothing
                } else if ("expression".equals(name)) {
                    throw new BuilderException("Expression based parameters are not supported yet");
                } else {
                    throw new BuilderException("An invalid property '" + name +
                            "' was found in mapping #{" + content + "}.  Valid properties are " + Arrays.toString(PARAMETER_PROPERTIES));
                }
            }
            if (typeHandlerAlias != null) {
                builder.typeHandler(super.resolveTypeHandler(javaType, typeHandlerAlias));
            }
            return builder.build();
        }

        private Map<String, String> parseParameterMapping(String content) {
            try {
                return new ParameterExpression(content);
            } catch (BuilderException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new BuilderException("Parsing error was found in mapping #{" + content + "}.  Check syntax #{property|(expression), var1=value1, var2=value2, ...} ", ex);
            }
        }
    }
}

