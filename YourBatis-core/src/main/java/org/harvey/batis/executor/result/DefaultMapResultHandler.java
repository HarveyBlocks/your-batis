package org.harvey.batis.executor.result;

import lombok.Getter;
import org.harvey.batis.reflection.MetaObject;
import org.harvey.batis.reflection.ReflectorFactory;
import org.harvey.batis.reflection.factory.ObjectFactory;
import org.harvey.batis.reflection.wrapper.ObjectWrapperFactory;

import java.util.Map;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-14 22:06
 * @see #DefaultMapResultHandler(String, ObjectFactory, ObjectWrapperFactory, ReflectorFactory)
 */
public class DefaultMapResultHandler<K, V> implements ResultHandler<V> {
    private final String mapKey;
    @Getter
    private final Map<K, V> mappedResults;
    private final ObjectFactory objectFactory;
    private final ObjectWrapperFactory objectWrapperFactory;
    private final ReflectorFactory reflectorFactory;

    /**
     * @param mapKey               全配置名, 如school.Students[12].score.math[2].value<br>
     *                             该名称用来从映射完之后的实体中依据mapKey对应的Getter, <br>
     *                             这个Getter返回这个实例对象的唯一标识, 然后所有对象将以这个唯一标识为键, <br>
     *                             存入{@link #mappedResults}中
     * @param objectFactory        为了在{@link #handleResult(ResultContext)}使用
     *                             {@link MetaObject#forObject(Object, ObjectFactory, ObjectWrapperFactory, ReflectorFactory)}
     *                             创建Object
     * @param objectWrapperFactory 为了在{@link #handleResult(ResultContext)}使用
     *                             {@link MetaObject#forObject(Object, ObjectFactory, ObjectWrapperFactory, ReflectorFactory)}
     *                             创建Object
     * @param reflectorFactory     为了在{@link #handleResult(ResultContext)}使用
     *                             {@link MetaObject#forObject(Object, ObjectFactory, ObjectWrapperFactory, ReflectorFactory)}
     *                             创建Object
     */
    public DefaultMapResultHandler(String mapKey, ObjectFactory objectFactory,
                                   ObjectWrapperFactory objectWrapperFactory,
                                   ReflectorFactory reflectorFactory) {
        this.objectFactory = objectFactory;
        this.objectWrapperFactory = objectWrapperFactory;
        this.reflectorFactory = reflectorFactory;
        this.mappedResults = objectFactory.create(Map.class);
        this.mapKey = mapKey;
    }

    /**
     * 从context中获取值<br>
     * 反射, 获取{@link #mapKey}全配置名对应的Field的值, 作为key<br>
     * 从context中获取到的值作为{@link #mappedResults}的value<br>
     *
     * @param context 需要被解决的结果
     */
    @Override
    public void handleResult(ResultContext<? extends V> context) {
        final V value = context.getResultObject();
        final MetaObject mo = MetaObject.forObject(value, objectFactory, objectWrapperFactory, reflectorFactory);
        final K key = (K) mo.getValue(mapKey);
        mappedResults.put(key, value);
    }

}
