package org.harvey.batis.executor.result;

import org.harvey.batis.config.Configuration;
import org.harvey.batis.exception.executor.ExecutorException;
import org.harvey.batis.reflection.MetaObject;
import org.harvey.batis.reflection.factory.ObjectFactory;

import java.lang.reflect.Array;
import java.util.List;

/**
 * TODO
 * 结果提取
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-14 22:15
 */
public class ResultExtractor {
    private final Configuration configuration;
    private final ObjectFactory objectFactory;

    public ResultExtractor(Configuration configuration, ObjectFactory objectFactory) {
        this.configuration = configuration;
        this.objectFactory = objectFactory;
    }


    /**
     * 从List中提取目标类型
     *
     * @param list       集合
     * @param targetType 目标类型, 如果不是集合/数组类型, 就返回null
     * @return 提取到的类型
     */
    public Object extractFromList(List<Object> list, Class<?> targetType) {
        if (targetType == null) {
            return getOne(list);
        }
        // targetType是list实体的父类
        if (targetType.isAssignableFrom(list.getClass())) {
            // 返回list整体
            return list;
        }
        // targetType是集合
        if (objectFactory.isCollection(targetType)) {
            // 利用反射创建targetType实体
            Object value = objectFactory.create(targetType);
            MetaObject metaObject = configuration.newMetaObject(value);
            // 利用反射将list内的元素拷贝到targetType类型的实体
            metaObject.addAll(list);
            // 然后返回
            return value;
        }
        // targetType不是数组
        if (!targetType.isArray()) {
            return null;
        }
        // 元素类型
        Class<?> arrayComponentType = targetType.getComponentType();
        // 实例化数组
        Object array = Array.newInstance(arrayComponentType, list.size());
        if (!arrayComponentType.isPrimitive()) {
            // 数据不是原始的(原始类型[boolean, int, char, ...], 引用类型)
            // 拷贝元素后返回
            return list.toArray((Object[]) array);
        }
        // 数据是原始的
        for (int i = 0; i < list.size(); i++) {
            Array.set(array, i, list.get(i));
        }
        return array;
    }

    private static Object getOne(List<Object> list) {
        if (list == null) {
            return null;
        }
        if (list.isEmpty()) {
            return null;
        }
        if (list.size() > 1) {
            throw new ExecutorException("Statement returned more than one row, where no more than one was expected.");
        }
        return list.get(0);
    }
}
