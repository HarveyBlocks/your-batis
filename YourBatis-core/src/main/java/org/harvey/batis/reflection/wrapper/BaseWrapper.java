package org.harvey.batis.reflection.wrapper;

import org.harvey.batis.exception.reflection.ReflectionException;
import org.harvey.batis.reflection.MetaObject;
import org.harvey.batis.reflection.property.PropertyTokenizer;

import java.util.List;
import java.util.Map;

/**
 * 判断本体类型并分流
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-25 09:59
 */
public abstract class BaseWrapper implements ObjectWrapper {
    protected static final Object[] NO_ARGUMENTS = new Object[0];
    protected final MetaObject metaObject;

    protected BaseWrapper(MetaObject metaObject) {
        this.metaObject = metaObject;
    }

    /**
     * 从MetaObject中获取值
     *
     * @param prop {@code prop.getIndex() != null}
     * @param defaultValue 当出现school.[12].score的情况, 不知道[12]的name是啥的时候返回defaultValue
     */
    protected Object resolveCollection(PropertyTokenizer prop, Object defaultValue) {
        String propName = prop.getName();
        if (propName.isEmpty()) {
            // school.[12].score
            // (?) 不知道啥情况
            return defaultValue; // 找不到配置
        } else {
            // 从字段`metaObject` 中获取值,
            // 其中, metaObject中保存具体对象, 会引导向真正的获取值的方法
            return metaObject.getValue(propName);
        }
    }

    /**
     * @param collection 从中获取prop的元素
     * @throws IndexOutOfBoundsException prop中的index如果超了, 会抛出异常
     */
    protected Object getCollectionValue(PropertyTokenizer prop, Object collection) {
        if (collection instanceof Map) {
            // prop.getIndex()的类型是String, 要求collection的值要一定是String才行
            return ((Map<?, ?>) collection).get(prop.getIndex());
            // 这里的index不一定是数字索引, 这里的是字符串, 也可以是"李四"之流
        }
        // 在去除了collection之后, Index就只能是Integer了, 其他集合没这么好使
        int i = Integer.parseInt(prop.getIndex());
        if (collection instanceof List) {
            return ((List<?>) collection).get(i);
        } else if (collection instanceof Object[]) {
            return ((Object[]) collection)[i];
        } else if (collection instanceof char[]) {
            return ((char[]) collection)[i];
        } else if (collection instanceof boolean[]) {
            return ((boolean[]) collection)[i];
        } else if (collection instanceof byte[]) {
            return ((byte[]) collection)[i];
        } else if (collection instanceof double[]) {
            return ((double[]) collection)[i];
        } else if (collection instanceof float[]) {
            return ((float[]) collection)[i];
        } else if (collection instanceof int[]) {
            return ((int[]) collection)[i];
        } else if (collection instanceof long[]) {
            return ((long[]) collection)[i];
        } else if (collection instanceof short[]) {
            return ((short[]) collection)[i];
        } else {
            throw new ReflectionException(
                    "The '" + prop.getName() +
                            "' property of " + collection + " is not a List or Array.");
        }

    }

    /**
     * @param collection 从中获取prop的元素, 并赋值value
     */
    protected void setCollectionValue(PropertyTokenizer prop, Object collection, Object value) {
        if (collection instanceof Map) {
            ((Map<String, Object>) collection).put(prop.getIndex(), value);
            return;
        }
        int i = Integer.parseInt(prop.getIndex());
        if (collection instanceof List) {
            ((List<Object>) collection).set(i, value);
        } else if (collection instanceof Object[]) {
            ((Object[]) collection)[i] = value;
        } else if (collection instanceof char[]) {
            ((char[]) collection)[i] = (Character) value;
        } else if (collection instanceof boolean[]) {
            ((boolean[]) collection)[i] = (Boolean) value;
        } else if (collection instanceof byte[]) {
            ((byte[]) collection)[i] = (Byte) value;
        } else if (collection instanceof double[]) {
            ((double[]) collection)[i] = (Double) value;
        } else if (collection instanceof float[]) {
            ((float[]) collection)[i] = (Float) value;
        } else if (collection instanceof int[]) {
            ((int[]) collection)[i] = (Integer) value;
        } else if (collection instanceof long[]) {
            ((long[]) collection)[i] = (Long) value;
        } else if (collection instanceof short[]) {
            ((short[]) collection)[i] = (Short) value;
        } else {
            throw new ReflectionException("The '" + prop.getName() + "' property of " +
                    collection + " is not a List or Array.");
        }

    }

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    public void add(Object element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <E> void addAll(List<E> element) {
        throw new UnsupportedOperationException();
    }

}
