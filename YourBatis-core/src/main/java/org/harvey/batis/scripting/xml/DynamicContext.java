package org.harvey.batis.scripting.xml;

import org.harvey.batis.config.Configuration;
import org.harvey.batis.reflection.ContextMap;
import org.harvey.batis.reflection.MetaObject;
import org.harvey.batis.scripting.js.LanguagePhaser;
import org.harvey.batis.scripting.js.PropertyAccessor;

import java.util.Map;
import java.util.StringJoiner;

/**
 * 组装sql语句
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-11 23:38
 */
public class DynamicContext {

    public static final String PARAMETER_OBJECT_KEY = "_parameter";

    static {
        LanguagePhaser.setPropertyAccessor(ContextMap.class, new ContextAccessor());
    }

    private final ContextMap bindings;
    /**
     * <pre>{@code
     * StringJoiner sj = new StringJoiner(",");
     * sj.add("apple");
     * sj.add("banana");
     * sj.add("orange");
     * String result = sj.toString();
     * // "apple,banana,orange"
     * }</pre>
     */
    private final StringJoiner sqlJoiner = new StringJoiner(" ");
    private int uniqueNumber = 0;

    public DynamicContext(Configuration configuration, Object parameterObject) {
        MetaObject metaObject = null;
        if (parameterObject != null && !(parameterObject instanceof Map)) {
            metaObject = configuration.newMetaObject(parameterObject);
        }
        bindings = new ContextMap(metaObject);
        bindings.put(PARAMETER_OBJECT_KEY, parameterObject);
    }


    public String getSql() {
        return sqlJoiner.toString().trim();
    }

    public Map<String, Object> getBindings() {
        return this.bindings;
    }

    public void appendSql(String sql) {
        sqlJoiner.add(sql);
    }

    public void bind(String name, Object value) {
        bindings.put(name, value);
    }

    /**
     * 类似于id, 每次调用, 返回值都会增大1
     *
     * @return {@link #uniqueNumber}++
     */
    public int getUniqueNumber() {
        return uniqueNumber++;
    }

    static class ContextAccessor implements PropertyAccessor {

        @Override
        public Object getProperty(Map<?, ?> ignored, Object target, Object name) {
            Map<?, ?> map = (Map<?, ?>) target;

            Object result = map.get(name);
            if (map.containsKey(name) || result != null) {
                return result;
            }

            Object parameterObject = map.get(PARAMETER_OBJECT_KEY);
            if (parameterObject instanceof Map) {
                return ((Map<?, ?>) parameterObject).get(name);
            }

            return null;
        }

        @Override
        public void setProperty(Map<?, ?> ignored, Object target, Object name, Object value) {
            Map<Object, Object> map = (Map<Object, Object>) target;
            map.put(name, value);
        }
    }
}
