package org.harvey.batis.reflection.wrapper;


import org.harvey.batis.reflection.MetaObject;
import org.harvey.batis.reflection.SystemMetaObject;
import org.harvey.batis.reflection.factory.ObjectFactory;
import org.harvey.batis.reflection.property.PropertyTokenizer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * 对Map类型的包装<br/>
 * 对于一个Map, 其字段可以看作其所有的Key, 可写, 可读<br/>
 * 而且由于Map的元素数量是动态的, Key的数量随时可以增删<br/>
 * 🤔 : Map的key, 要不要考虑配置的大小写? 是否要将配置名的匹配作为忽略大小写的形式?
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-24 12:55
 */
public class MapWrapper extends BaseWrapper {

    private final Map<String, Object> map;

    public MapWrapper(MetaObject metaObject, Map<String, Object> map) {
        super(metaObject);
        this.map = map;
    }

    /**
     * @param prop 要从Map里获取, prop的表达有两中方式<br/>
     *             math.Feb; math[Feb]<br/>
     *             都表示数学二月的成绩
     */
    @Override
    public Object get(PropertyTokenizer prop) {
        if (prop.getIndex() == null) {
            // 需要的只是普通的Bean, 哪值就从Map里取
            return map.get(prop.getName());
        }
        // 需要的是一个集合
        Object collection = super.resolveCollection(prop, map);
        // 如果触发了resolveCollection的defaultValue, collection还是map的话
        // 那么getCollectionValue也是map.get(prop.getName)
        return super.getCollectionValue(prop, collection);
    }

    /**
     * 要向Map里写入
     *
     * @see MapWrapper#get(PropertyTokenizer)
     */
    @Override
    public void set(PropertyTokenizer prop, Object value) {
        // 当前为school, 当prop为: students[12].name, value为List时
        if (prop.getIndex() == null) {
            map.put(prop.getName(), value);
            return;
        }
        Object collection = super.resolveCollection(prop, map);
        super.setCollectionValue(prop, collection, value);
    }


    /**
     * @see MapWrapper#getMethodType
     */
    @Override
    public Class<?> getSetterType(String name) {
        return getMethodType(name, MetaObject::getSetterType);
    }


    /**
     * @see MapWrapper#getMethodType
     */
    @Override
    public Class<?> getGetterType(String name) {
        return getMethodType(name, MetaObject::getGetterType);
    }

    @FunctionalInterface
    private interface MetaValueGetMethodTypeBiFunction extends
            BiFunction<MetaObject, String, Class<?>> {
    }

    /**
     * 获取元素的类型(如果元素存在),<br/>
     * 配置不是最后一层, 就从map中获取了值之后进行递归解析<br/>
     * 配置是最后一层, 从map中获取元素, 然后返回元素类型<br/>
     *
     * @return 配置是最后一层, 从map中获取元素, 如果元素不存在, 返回Object.class, <br/>
     * 因为认为Map中存在无穷多个配置(涵盖所有配置), 即使现在没有这个配置, 以后也可以有, <br/>
     * 故不认为这个配置不存在, 不抛出异常
     */
    private Class<?> getMethodType(String name,
                                   MetaValueGetMethodTypeBiFunction metaValueGetMethodType) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (!prop.hasNext()) {
            // 递归出口
            Object value = map.get(name);
            return value == null ? Object.class : value.getClass();
        }
        MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
        return metaValue == SystemMetaObject.NULL_META_OBJECT ? Object.class :
                // 递归
                metaValueGetMethodType.apply(metaValue, prop.getChildrenFullname());
    }


    /**
     * get的过程和set不同, 需要层层解析配置名, 然后从众多map中获取
     *
     * @param name 配置名
     */
    @Override
    public boolean hasGetter(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (!prop.hasNext()) {
            // 最后一层解析
            // 递归出口
            return map.containsKey(prop.getName());
        }
        if (!map.containsKey(prop.getIndexedName())) {
            // 🤔 : 为什么是IndexedName 而不是name?
            //      当param的name为students[12].score, 当前对象是school时,
            //      students[12]是一个对象, students是一个集合
            //      值得注意的是: Map中含有的成员是students, 而不是students[12]中的元素student
            //      故希望返回false
            return false;
        }
        // 实例化子对象
        // 不存在配置metaObjectForProperty抛出异常
        MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
        if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
            // 实例化之后是null
            // 也认为存在Getter, 因为不存在的话, metaObjectForProperty会抛出异常
            return true;
        }
        // 递归, 用子对象进行进一步的解析
        return metaValue.hasGetter(prop.getChildrenFullname());
    }

    /**
     * map的put方法, 可以加入新元素, 意味着name的配置一定是可写的
     */
    @Override
    public boolean hasSetter(String name) {
        return true;
    }

    /**
     * @param name 无用
     * @return 新的Map, 作为当前Map的元素, 包装成MetaObject后返回
     */
    @Override
    public MetaObject instantiatePropertyValue(String name,
                                               PropertyTokenizer prop,
                                               ObjectFactory objectFactory) {
        Map<String, Object> map = new HashMap<>();
        // 存入当前Map
        this.set(prop, map);
        // 生成包装后的MetaObject
        return MetaObject.forObject(map,
                metaObject.getObjectFactory(),
                metaObject.getObjectWrapperFactory(),
                metaObject.getReflectorFactory());
    }


    /**
     * 对于一个Map, 其字段可以看作其所有的Key, 可写, 可读<br/>
     *
     * @param name 对于参数,无论参数是什么形式, 如果希望将参数写入Map, 总是可写的, <br/>
     *             且不会有解析, 因为每一层解析都可以是任意形式的name配置名
     * @return 所以直接返回参数中的原生name
     */
    @Override
    public String findProperty(String name, boolean useCamelCaseMapping) {
        return name;
    }

    /**
     * @see MapWrapper#getMethodNames()
     */
    @Override
    public String[] getGetterNames() {
        return this.getMethodNames();
    }

    /**
     * @see MapWrapper#getMethodNames()
     */
    @Override
    public String[] getSetterNames() {
        return this.getMethodNames();
    }

    /**
     * 对于一个Map, 其字段可以看作其所有的Key, 可写, 可读<br/>
     * 所以直接返回map字段的所有key
     */
    private String[] getMethodNames() {
        // IDEA推荐使用String[0], toArray方法有自定新创建数组的大小的作用
        return map.keySet().toArray(new String[/*map.keySet().size()*/0]);
    }


}
