package org.harvey.batis.mapping;

import lombok.Getter;
import org.harvey.batis.config.Configuration;
import org.harvey.batis.executor.key.generator.Jdbc3KeyGenerator;
import org.harvey.batis.executor.key.generator.KeyGenerator;
import org.harvey.batis.executor.key.generator.NoKeyGenerator;
import org.harvey.batis.io.log.Log;
import org.harvey.batis.io.log.LogFactory;
import org.harvey.batis.mapping.sqlsource.SqlSource;
import org.harvey.batis.scripting.LanguageDriver;
import org.harvey.batis.util.ArrayUtil;
import org.harvey.batis.util.enums.ResultSetType;
import org.harvey.batis.util.enums.SqlCommandType;
import org.harvey.batis.util.enums.StatementType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TODO
 * 包含从XMl解析的SQL语句的信息
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-02 15:11
 */
@Getter
public class MappedStatement {


    private MappedStatement() {
        // constructor disabled
    }

    private String id;
    private SqlCommandType sqlCommandType;
    private String resource;
    private Configuration configuration;
    private Integer fetchSize;
    private Integer timeout;
    private StatementType statementType;
    private SqlSource sqlSource;
    private ResultSetType resultSetType;
    private KeyGenerator keyGenerator;
    private String[] keyColumns;
    // 不可写
    private Log statementLog;
    private LanguageDriver lang;
    private ParameterMap parameterMap;
    private List<ResultMap> resultMaps;
    private String[] resultSets;

    private boolean flushCacheRequired;

    @Deprecated
    private boolean hasNestedResultMaps;

    /**
     * 解析param, 绑定参数位置和参数值<br>
     * 参数值依据{@link StatementType}进行调整, 以防注入
     *
     * @param parameterObject 具体含有值的对象(含有的是Mapper接口代理传入的值)
     * @return 绑定了参数位置和参数值和SQL语句的BoundSql
     * @see BoundSql
     */
    public BoundSql getBoundSql(Object parameterObject) {
        // 获取解析Sql语句的类
        BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
        // 参数映射列表
        // 从XML解析出来的需要的参数
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings == null || parameterMappings.isEmpty()) {
            // 如果没有参数映射列表
            // throw new UnfinishedFunctionException("未准备parameterMap");
            boundSql = new BoundSql(configuration, boundSql.getSql(),
                    parameterMap.getParameterMappings(), parameterObject);
        }

        // 检查参数映射中的嵌套结果映射
        // 认为没有
        for (ParameterMapping pm : boundSql.getParameterMappings()) {
            String rmId = pm.getResultMapId();
            if (rmId == null) {
                continue;
            }
            ResultMap rm = configuration.getResultMap(rmId);
            if (rm != null) {
                hasNestedResultMaps |= rm.isHasNestedResultMaps();
            }
        }

        return boundSql;
    }


    public static class Builder {
        private final MappedStatement product = new MappedStatement();

        public Builder(Configuration configuration, String statementId, SqlSource sqlSource, SqlCommandType sqlCommandType) {
            product.configuration = configuration;
            product.id = statementId;
            product.sqlSource = sqlSource;
            product.statementType = StatementType.PREPARED;
            product.resultSetType = ResultSetType.DEFAULT;
            product.parameterMap = new ParameterMap.Builder("defaultParameterMap", null, new ArrayList<>()).build();
            product.resultMaps = new ArrayList<>();
            product.sqlCommandType = sqlCommandType;
            product.keyGenerator = configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType) ? Jdbc3KeyGenerator.INSTANCE : NoKeyGenerator.INSTANCE;
            product.statementLog = LogFactory.getLog(statementId);
            product.lang = configuration.getDefaultScriptingLanguageInstance();
        }

        public MappedStatement build() {
            assert product.configuration != null;
            assert product.id != null;
            assert product.sqlSource != null;
            assert product.lang != null;
            product.resultMaps = Collections.unmodifiableList(product.resultMaps);
            return product;
        }

        public Builder resource(String resource) {
            product.resource = resource;
            return this;
        }

        public Builder flushCacheRequired(boolean flushCacheRequired) {
            product.flushCacheRequired = flushCacheRequired;
            return this;
        }

        public Builder statementType(StatementType statementType) {
            product.statementType = statementType;
            return this;
        }

        public Builder lang(LanguageDriver lang) {
            product.lang = lang;
            return this;
        }


        public Builder fetchSize(Integer fetchSize) {
            product.fetchSize = fetchSize;
            return this;
        }

        public Builder timeout(Integer timeout) {
            product.timeout = timeout;
            return this;
        }

        public Builder resultSetType(ResultSetType resultSetType) {
            product.resultSetType = resultSetType == null ? ResultSetType.DEFAULT : resultSetType;
            return this;
        }

        public Builder resultMaps(List<ResultMap> resultMaps) {
            product.resultMaps = resultMaps;
            for (ResultMap resultMap : resultMaps) {
                product.hasNestedResultMaps = product.hasNestedResultMaps || resultMap.isHasNestedResultMaps();
            }
            return this;
        }

        public Builder parameterMap(ParameterMap parameterMap) {
            product.parameterMap = parameterMap;
            return this;
        }

        public Builder resultSets(String resultSets) {
            product.resultSets = ArrayUtil.splitTrimEach(resultSets, ',');
            return this;
        }

        public Builder keyGenerator(KeyGenerator keyGenerator) {
            product.keyGenerator = keyGenerator;
            return this;
        }

        public Builder keyColumn(String keyColumn) {
            product.keyColumns = MappedStatement.delimitedStringToArray(keyColumn);
            return this;
        }

    }

    private static String[] delimitedStringToArray(String in) {
        if (in == null || in.trim().isEmpty()) {
            return null;
        } else {
            return ArrayUtil.split(in, ',');
        }
    }
}
