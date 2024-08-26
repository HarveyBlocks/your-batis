package org.harvey.batis.mapping;

import lombok.Getter;
import org.harvey.batis.config.Configuration;
import org.harvey.batis.reflection.MetaObject;
import org.harvey.batis.reflection.ParamNameResolver;
import org.harvey.batis.reflection.property.PropertyTokenizer;
import org.harvey.batis.scripting.xml.DynamicContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO
 * 绑定了参数位置和参数值和SQL语句
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-11 23:57
 */
@Getter
public class BoundSql {
    /**
     * XML文件里的SQL
     */
    private final String sql;
    /**
     * 从XML解析出来的需要的参数
     */
    private final List<ParameterMapping> parameterMappings;
    /**
     * 解析Mapper接口而创建的参数值的Object, 可能是{@link ParamNameResolver.ParamMap}
     */
    private final Object parameterObject;
    /**
     * TODO 附加参数, 和解析SQL时的上下文有关
     */
    private final Map<String, Object> additionalParameters;
    /**
     * 用{@link #additionalParameters}创建的{@link MetaObject}
     */
    private final MetaObject metaParameters;


    public BoundSql(Configuration configuration, String sql, List<ParameterMapping> parameterMappings, Object parameterObject) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.parameterObject = parameterObject;


        this.additionalParameters = new HashMap<>();
        this.metaParameters = configuration.newMetaObject(additionalParameters);
    }


    /**
     * @param name reflect中的配置名
     * @return 含有, 则返回true
     */
    public boolean hasAdditionalParameter(String name) {
        String paramName = new PropertyTokenizer(name).getName();
        return additionalParameters.containsKey(paramName);
    }

    /**
     * @see DynamicContext#getBindings()
     */
    public void setAdditionalParameter(String name, Object value) {
        metaParameters.setValue(name, value);
    }

    public Object getAdditionalParameter(String name) {
        return metaParameters.getValue(name);
    }
}
