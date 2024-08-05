package org.harvey.batis.reflection.wrapper;

import org.harvey.batis.reflection.MetaObject;
import org.harvey.batis.reflection.factory.ObjectFactory;
import org.harvey.batis.reflection.property.PropertyTokenizer;

import java.util.List;


/**
 * 类型包装接口
 * 抽象了对象的字段信息、 getter/setter 方法
 * 使用类似于文件-文件夹结构的组合设计模式
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-24 11:18
 */
public interface ObjectWrapper {

    Object get(PropertyTokenizer prop);

    void set(PropertyTokenizer prop, Object value);

    String findProperty(String name, boolean useCamelCaseMapping);

    String[] getGetterNames();

    String[] getSetterNames();

    Class<?> getSetterType(String name);

    Class<?> getGetterType(String name);

    boolean hasSetter(String name);

    boolean hasGetter(String name);

    MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory);

    boolean isCollection();

    void add(Object element);

    <E> void addAll(List<E> element);
}
