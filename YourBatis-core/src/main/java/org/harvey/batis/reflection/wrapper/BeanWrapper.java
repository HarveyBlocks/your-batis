package org.harvey.batis.reflection.wrapper;


import org.harvey.batis.exception.reflection.ReflectionException;
import org.harvey.batis.reflection.MetaClass;
import org.harvey.batis.reflection.MetaObject;
import org.harvey.batis.reflection.SystemMetaObject;
import org.harvey.batis.reflection.factory.ObjectFactory;
import org.harvey.batis.reflection.invoke.Invoker;
import org.harvey.batis.reflection.property.PropertyTokenizer;
import org.harvey.batis.util.ReflectionExceptionUnwrappedMaker;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 对一般类型的包装
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-24 12:58
 */
public class BeanWrapper extends BaseWrapper {
    private final Object object;
    private final MetaClass metaClass;

    public BeanWrapper(MetaObject metaObject, Object object) {
        super(metaObject);
        this.object = object;
        this.metaClass = MetaClass.forClass(object.getClass(), metaObject.getReflectorFactory());
    }

    @Override
    public Object get(PropertyTokenizer prop) {
        if (prop.getIndex() == null) {
            return this.getBeanProperty(prop, object);
        }
        Object collection = super.resolveCollection(prop, object);
        return super.getCollectionValue(prop, collection);
    }

    private Object getBeanProperty(PropertyTokenizer prop, Object object) {
        try {
            Invoker method = metaClass.getGetInvoker(prop.getName());
            try {
                return method.invoke(object, NO_ARGUMENTS);
            } catch (Throwable t) {
                throw ReflectionExceptionUnwrappedMaker.unwrapThrowable(t);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new ReflectionException("Could not get property '" + prop.getName() + "' from " + object.getClass() + ".  Cause: " + t, t);
        }
    }

    @Override
    public void set(PropertyTokenizer prop, Object value) {
        if (prop.getIndex() == null) {
            this.setBeanProperty(prop, object, value);
            return;
        }
        Object collection = resolveCollection(prop, object);
        this.setCollectionValue(prop, collection, value);
    }

    private void setBeanProperty(PropertyTokenizer prop, Object object, Object value) {
        try {
            Invoker method = metaClass.getSetInvoker(prop.getName());
            Object[] params = {value};
            try {
                method.invoke(object, params);
            } catch (Throwable t) {
                throw ReflectionExceptionUnwrappedMaker.unwrapThrowable(t);
            }
        } catch (Throwable t) {
            throw new ReflectionException("Could not set property '" + prop.getName() + "' of '" + object.getClass() + "' with value '" + value + "' Cause: " + t, t);
        }
    }

    @Override
    public String findProperty(String name, boolean useCamelCaseMapping) {
        return metaClass.findProperty(name, useCamelCaseMapping);
    }


    @Override
    public String[] getGetterNames() {
        return metaClass.getGetterNames();
    }

    @Override
    public String[] getSetterNames() {
        return metaClass.getSetterNames();
    }


    /**
     * 从Setter递归获取当前对象的配置名对应的配置的类型对象<br>
     *
     * @param name 如student[12].name, 假设当前对象是school, 含有依赖student
     * @return name对应的配置的类型, 即score的类型
     */
    @Override
    public Class<?> getSetterType(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (!prop.hasNext()) {
            // 来到了配置的末尾, 接下来没有配置了
            // 递归出口, 直接从该实例对象的类对象中获取配置(name)对应的Setter的类型
            return metaClass.getSetterType(name);
        }
        MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
        if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
            // 配置名如果是Null配置, 表示配置链中断, 最终也不能往下去获取配置实例对象了
            // 递归出口, 直接从该实例对象的类对象中获取配置(name)对应的Setter的类型
            return metaClass.getSetterType(name);
        }
        // 正常的情况
        // 就是要往当前对象的配置的配置的类型
        // 构成递归
        return metaValue.getSetterType(prop.getChildrenFullname());
    }

    /**
     * 从Getter递归获取当前对象的配置名对应的配置的类型对象<br/>
     *
     * @param name 如student[12].score, 假设当前对象是school, 含有依赖student
     * @return name对应的配置的类型, 即score的类型, 如果解析到如student[12]这种集合的时候<br/>
     * 如果需要的是元素(当然也有可能需要的是集合本身, 例如配置名为school.student.length),<br/>
     * 则调用{@link MetaClass#getGetterType(String)}, 解析泛型获取集合的元素类型
     * @see MetaClass#getGetterType(String)
     */
    @Override
    public Class<?> getGetterType(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (!prop.hasNext()) {
            return metaClass.getGetterType(name);
        }
        MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
        if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
            return metaClass.getGetterType(name);
        }
        return metaValue.getGetterType(prop.getChildrenFullname());
    }

    /**
     * 从当前对象的MetaClass对象, 解析Setter配置, 然后返回最终配置是否存在
     *
     * @return true 表示存在
     */
    @Override
    public boolean hasSetter(String fullname) {
        return this.hasMethod(fullname, metaClass::hasSetter, MetaObject::hasSetter);
    }

    /**
     * 从当前对象的MetaClass对象, 解析Getter配置, 然后返回最终配置是否存在
     *
     * @return true 表示存在
     */
    @Override
    public boolean hasGetter(String fullname) {
        return this.hasMethod(fullname, metaClass::hasGetter, MetaObject::hasGetter);
    }


    @FunctionalInterface
    private interface MetaClassHasMethodFunction extends Function<String, Boolean> {
    }

    @FunctionalInterface
    private interface MetaValueHasMethodBiFunction extends BiFunction<MetaObject, String, Boolean> {
    }

    private boolean hasMethod(String fullname,
                              MetaClassHasMethodFunction metaClassHasMethod,
                              MetaValueHasMethodBiFunction metaValueHasMethod) {
        PropertyTokenizer prop = new PropertyTokenizer(fullname);
        // school.students[12].score.math
        // 当前MetaObject为school, 则fullname = students[12].score.math
        if (!prop.hasNext()) {
            // 递归出口
            return metaClassHasMethod.apply(fullname);
        }
        // students[12]存在, 这里无法检查IndexOutOfBound的情况
        if (!metaClassHasMethod.apply(prop.getIndexedName())) {
            return false;
        }
        // 存在students
        // 获取students的MetaObject, 这里能判断IndexOutOfBound的情况(抛出)
        // metaValue即使students中索引为12的那个student
        MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
        if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
            // 解析子成员由于null中断了
            // 开始解析当前一级的成员
            // 递归出口, 同上一个递归出口
            return metaClassHasMethod.apply(fullname);
        }
        // 从student中获取score.math成员对象
        // 构成递归
        return metaValueHasMethod.apply(metaValue, prop.getChildrenFullname());
    }


    /**
     * 实例化并赋值prop
     *
     * @param propertyName  配置名, 若当前为school, 则配置的name为students[12].score.math[2].value<br/>
     *                      作用是仅仅在出现异常的时候记录异常信息
     * @param prop          依据该配置,获取需要实例化的配置的类型, 实例化, 包装成{@link MetaObject}并赋值
     * @param objectFactory 指定需要实例化该配置的时候, 使用的是哪个工厂
     * @return 实例化并赋值后的对象
     */
    @Override
    public MetaObject instantiatePropertyValue(String propertyName,
                                               PropertyTokenizer prop,
                                               ObjectFactory objectFactory) {
        MetaObject metaValue;
        // 判断该配置是否可写
        Class<?> type = this.getSetterType(prop.getName());
        try {
            // 实例化配置
            Object newObject = objectFactory.create(type);
            // 包装配置
            metaValue = MetaObject.forObject(newObject,
                    metaObject.getObjectFactory(),
                    metaObject.getObjectWrapperFactory(),
                    metaObject.getReflectorFactory());
            // 赋值
            this.set(prop, newObject);
        } catch (Exception e) {
            throw new ReflectionException("Cannot set value of property '" + propertyName +
                    "' because '" + propertyName +
                    "' is null and cannot be instantiated on instance of " + type.getName() +
                    ". Cause:" + e, e);
            // “无法设置属性 'name' 的值，因为 'name' 为 null，无法在 type 的实例上实例化。原因：e”
        }
        return metaValue;
    }
}
