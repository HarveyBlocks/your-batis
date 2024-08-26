package org.harvey.batis.reflection;

import org.harvey.batis.annotation.Param;
import org.harvey.batis.config.Configuration;
import org.harvey.batis.exception.binding.BindingException;
import org.harvey.batis.executor.result.ResultHandler;
import org.harvey.batis.session.RowBounds;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 解析@Param注解
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-18 19:12
 */
public class ParamNameResolver {

    public static final String GENERIC_NAME_PREFIX = "param";

    private final boolean useActualParamName;

    /**
     * key: value = paramIndex: paramName<br>
     * value优先级如下:<br>
     * <ul>
     * <li>忽略Special的类型的参数{@link #isSpecialParameter(Class)}</li>
     * <li>{@link Param}里面的值</li>
     * <li>parameter原来的名字. <br>
     * 由于解析的是字节码文件, 也就是说, 获取到的是arg1, arg2这种名字, <br>
     * 建议不要出现这种情况</li>
     * </ul>
     * key 的值依据在方法的参数列表的位置索引, 不会因为{@link #isSpecialParameter(Class)}就移动索引位置
     */
    private final SortedMap<Integer, String> names;

    private boolean hasParamAnnotation;

    public ParamNameResolver(Configuration config, Method method) {
        this.useActualParamName = config.isUseActualParamName();
        final Class<?>[] paramTypes = method.getParameterTypes();
        final Annotation[][] paramAnnotations = method.getParameterAnnotations();
        // 参数经解析后的结果
        final SortedMap<Integer, String> map = new TreeMap<>();
        int paramCount = paramAnnotations.length;
        // 获取那些被@param注解的参数, 并获取参数的名字
        for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
            // 扫描参数列表
            if (isSpecialParameter(paramTypes[paramIndex])) {
                // 跳过特殊的参数
                continue;
            }
            String name = null;
            // 扫描当前参数的注解
            for (Annotation annotation : paramAnnotations[paramIndex]) {
                if (annotation instanceof Param) {
                    hasParamAnnotation = true;
                    name = ((Param) annotation).value();
                    break;
                }
            }
            if (name != null) {
                // 直接获取name并继续
                map.put(paramIndex, name);
                continue;
            }
            // @Param未具体说明
            if (useActualParamName) {
                // 获取具体的参数名
                name = this.getActualParamName(method, paramIndex);
            }
            if (name == null) {
                // 用数字代替参数名
                // 即当前参数在非特殊(isSpecial==false)参数列表中的位置索引
                name = String.valueOf(map.size());
            }
            map.put(paramIndex, name);
        }
        // 将结果的集合变成不可写的
        names = Collections.unmodifiableSortedMap(map);
    }

    /**
     * 是否是以下两类: {@link RowBounds}或{@link ResultHandler}或其子类<br>
     * 是, 则认为是特殊的参数
     *
     * @return 是则返回true
     */
    private static boolean isSpecialParameter(Class<?> type) {
        return RowBounds.class.isAssignableFrom(type) || ResultHandler.class.isAssignableFrom(type);
    }


    /**
     * @return 获取method的paramIndex位置上的参数的参数名
     */
    private String getActualParamName(Method method, int paramIndex) {
        return ParamNameUtil.getParamName(method, paramIndex);
    }

    /**
     * <p>
     * 将以一个单独的, 无名的参数(Object)的形式返回
     * 使用命名规则命名多个参数
     * </p>
     *
     * @param args 函数参数
     * @return SqlCommand的参数
     */
    public Object getNamedParams(Object[] args) {
        // 获取函数参数(非特殊的参数)个数
        final int paramCount = names.size();
        if (args == null || paramCount == 0) {
            return null;
        }
        if (!hasParamAnnotation && paramCount == 1) {
            // 非特殊的参数有一个, 且这个参数名不是依据@Param来的, 是直接用方法参数名来的
            Object value = args[names.firstKey()];
            // 如果是, 获取参数
            String actualParamName = useActualParamName ? names.get(names.firstKey()) : null;
            // 如果参数只有一个, 给这个参数加上默认别名
            return wrapToMapIfCollection(value, actualParamName);
        }
        final Map<String, Object> param = new ParamMap<>();
        int i = 0;
        for (Map.Entry<Integer, String> entry : names.entrySet()) {
            param.put(entry.getValue(), args[entry.getKey()]);
            final String genericParamName = GENERIC_NAME_PREFIX + (i + 1);
            if (!names.containsValue(genericParamName)) {
                param.put(genericParamName, args[entry.getKey()]);
            }
            i++;
        }
        return param;
    }

    /**
     * <li>如果object是null返回null</li>
     * <li>如果object是Collection集合或Array数组, 就包装成Map</li>
     * <li>如果object是entity, 就直接返回本身</li>
     * <li>如果object是map, 也直接返回本身</li>
     *
     * @param object          参数实体值本身
     * @param actualParamName 在定义接口的时候, 或依据@Param, 或依据参数名来决定的ParamName
     * @return {@link ParamMap} key: value = xml文件中的名字: argValue
     */
    public static Object wrapToMapIfCollection(Object object, String actualParamName) {
        if (object == null) {
            return null;
        }
        if (!object.getClass().isArray() && !(object instanceof Collection)) {
            // object不是Array类型的也不是集合
            // 就认为是entity类型的
            return object;
        }
        // 是集合或数组
        ParamMap<Object> map = new ParamMap<>();
        if (object instanceof Collection) {// 是集合
            // 加入默认的元素
            map.put("collection", object);
            if (object instanceof List) {
                map.put("list", object);
            }
        } else {
            // object是Array类型的
            // 默认的元素
            map.put("array", object);
        }
        // 依据参数名加入元素
        Optional.ofNullable(actualParamName)
                // actualParamName不为null
                .ifPresent(name -> map.put(name, object));
        return map;
    }

    /**
     * key: value = 参数名: 参数值
     * Get一个不存在的元素会抛出异常
     *
     * @param <V> 参数值
     */
    public static class ParamMap<V> extends HashMap<String, V> {

        @Override
        public V get(Object key) {
            if (!super.containsKey(key)) {
                throw new BindingException("Parameter '" + key + "' not found. Available parameters are " + keySet());
            }
            return super.get(key);
        }

    }
}