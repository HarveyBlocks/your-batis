package org.harvey.batis.reflection;

import lombok.Getter;
import org.harvey.batis.exception.reflection.ReflectionException;
import org.harvey.batis.reflection.factory.ObjectFactory;
import org.harvey.batis.reflection.property.PropertyTokenizer;
import org.harvey.batis.reflection.wrapper.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 反射工具类, 通过反射, 解析Getter和Setter, 获取对象的属性字段值
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-24 11:10
 */
@Getter
public class MetaObject {

    private final Object originalObject;
    private final ObjectWrapper objectWrapper;
    private final ObjectFactory objectFactory;
    private final ObjectWrapperFactory objectWrapperFactory;
    private final ReflectorFactory reflectorFactory;

    private MetaObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
        this.originalObject = object;
        this.objectFactory = objectFactory;
        this.objectWrapperFactory = objectWrapperFactory;
        this.reflectorFactory = reflectorFactory;

        if (object instanceof ObjectWrapper) {
            this.objectWrapper = (ObjectWrapper) object;
        } else if (objectWrapperFactory.hasWrapped(object)) {
            this.objectWrapper = objectWrapperFactory.wrap(this, object);
        } else if (object instanceof Map) {
            this.objectWrapper = new MapWrapper(this, (Map<String, Object>) object);
        } else if (object instanceof Collection) {
            this.objectWrapper = new CollectionWrapper((Collection<Object>) object);
        } else {
            this.objectWrapper = new BeanWrapper(this, object);
        }
    }

    public static MetaObject forObject(Object object, ObjectFactory objectFactory,
                                       ObjectWrapperFactory objectWrapperFactory,
                                       ReflectorFactory reflectorFactory) {
        if (object == null) {
            return SystemMetaObject.NULL_META_OBJECT;
        } else {
            return new MetaObject(object, objectFactory, objectWrapperFactory, reflectorFactory);
        }
    }

    public String findProperty(String propName, boolean useCamelCaseMapping) {
        return objectWrapper.findProperty(propName, useCamelCaseMapping);
    }

    public String[] getGetterNames() {
        return objectWrapper.getGetterNames();
    }

    public String[] getSetterNames() {
        return objectWrapper.getSetterNames();
    }

    public Class<?> getSetterType(String name) {
        // 进入一个递归获取配置类型的方法
        return objectWrapper.getSetterType(name);
    }

    public Class<?> getGetterType(String name) {
        return objectWrapper.getGetterType(name);
    }


    public boolean hasSetter(String name) {
        return objectWrapper.hasSetter(name);
    }

    public boolean hasGetter(String name) {
        return objectWrapper.hasGetter(name);
    }

    /**
     * @param fullname 全配置名, 如school->students[12]->score->math[2]->value
     * @return 从依据fullname成员中获取值, 但是当成员不存在呢?<br/>
     * 找不到的情况下抛出异常{@link ReflectionException}
     */
    public Object getValue(String fullname) {
        // school.students[12].score.math[2].value
        PropertyTokenizer prop = new PropertyTokenizer(fullname);
        if (!prop.hasNext()) {
            // 最后一层配置, 递归出口
            return objectWrapper.get(prop);
        }

        // 获取前缀中的值, 例如school
        MetaObject metaValue = this.metaObjectForProperty(
                prop.getIndexedName() // 获取数组名作为配置名
        );
        if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
            // 在中间层, 解析配置的过程中遇到null的配置
            // 终止递归
            return null;
        }
        // 向后递归
        return metaValue.getValue(prop.getChildrenFullname());


    }

    /**
     * 从当前实例对象(配置, 例如school), 获取配置名(param name)的配置, <br/>
     * 并将其实例化为对象{@link MetaObject#getValue}<br/>
     * 封装成MetaObject后返回
     *
     * @param name 配置名, 如students[12]
     * @return 实例化后的配置对象, 例如索引为12的student, 如果为Null, 则返回{@link SystemMetaObject#NULL_META_OBJECT}
     */
    public MetaObject metaObjectForProperty(String name) {
        Object value = this.getValue(name); // 获取配置值
        return MetaObject.forObject(value, objectFactory, objectWrapperFactory, reflectorFactory);
    }

    /**
     * @param fullname 全配置名, 如school.students[12].score.math[2].value.
     *                 更类似于上下文
     */
    public void setValue(String fullname, Object value) {
        PropertyTokenizer prop = new PropertyTokenizer(fullname);
        if (!prop.hasNext()) {
            objectWrapper.set(prop, value); // 进行实质赋值
            return;
        }
        // 获取配置前缀中的值, 例如school的值
        MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
        if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
            if (value == null) {
                // 在中间层, 解析配置的过程中遇到null的配置,
                // 所幸要赋值的值也是null, 没事
                return;
            } else {
                // 在中间层, 解析配置的过程中遇到null的配置,
                // 要赋值的值不是null? 实例化这个中间层
                metaValue = objectWrapper.instantiatePropertyValue(fullname, prop, objectFactory);
            }
        }
        // 向中间层传递值, 进行递归
        metaValue.setValue(prop.getChildrenFullname(), value);

    }


    public boolean isCollection() {
        return objectWrapper.isCollection();
    }

    public void add(Object element) {
        objectWrapper.add(element);
    }

    public <E> void addAll(List<E> list) {
        objectWrapper.addAll(list);
    }

}
