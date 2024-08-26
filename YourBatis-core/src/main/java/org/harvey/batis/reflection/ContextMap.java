package org.harvey.batis.reflection;

import java.util.HashMap;


/**
 * 一般的Map, 如果找不到,
 * 会从{@link #parameterMetaObject}中反射获取key(此时是properties)
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-24 13:15
 */
public class ContextMap extends HashMap<String, Object> {
    private final MetaObject parameterMetaObject;

    public ContextMap(MetaObject parameterMetaObject) {
        this.parameterMetaObject = parameterMetaObject;
    }

    /**
     * @param key 普通的key或者reflect中的properties的概念
     * @return map中存储的值, 如果不存在, 则解析properties对{@link #parameterMetaObject}进行反射
     */
    @Override
    public Object get(Object key) {
        String strKey = (String) key;
        if (super.containsKey(strKey)) {
            return super.get(strKey);
        }

        if (parameterMetaObject == null) {
            return null;
        }

        // issue #61 do not modify the context when reading
        return parameterMetaObject.getValue(strKey);
    }
}
