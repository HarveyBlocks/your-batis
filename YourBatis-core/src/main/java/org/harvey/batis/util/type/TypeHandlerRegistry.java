package org.harvey.batis.util.type;

import lombok.Getter;
import org.harvey.batis.config.Configuration;
import org.harvey.batis.exception.type.TypeException;
import org.harvey.batis.reflection.ParamNameResolver;
import org.harvey.batis.util.enums.JdbcType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 注册{@link TypeHandler}
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-15 00:31
 */
public class TypeHandlerRegistry {
    private static final Map<JdbcType, TypeHandler<?>> NULL_TYPE_HANDLER_MAP = Collections.emptyMap();
    private final Map<JdbcType, TypeHandler<?>> jdbcTypeHandlerMap = new EnumMap<>(JdbcType.class);
    private final Map<Type, Map<JdbcType, TypeHandler<?>>> typeHandlerMap = new ConcurrentHashMap<>();
    @Getter
    private final TypeHandler<Object> unknownTypeHandler;
    private final Map<Class<?>, TypeHandler<?>> allTypeHandlersMap = new HashMap<>();
    private Class<? extends TypeHandler> defaultEnumTypeHandler = EnumTypeHandler.class;

    public TypeHandlerRegistry(Configuration configuration) {
        // int
        this.register(Integer.class, new IntegerTypeHandler());
        this.register(int.class, new IntegerTypeHandler());
        this.register(JdbcType.INTEGER, new IntegerTypeHandler());
        // long
        this.register(Long.class, new LongTypeHandler());
        this.register(long.class, new LongTypeHandler());
        this.register(JdbcType.BIGINT, new LongTypeHandler());
        // bigint
        register(BigInteger.class, new BigIntegerTypeHandler());
        // double
        register(Double.class, new DoubleTypeHandler());
        register(double.class, new DoubleTypeHandler());
        register(JdbcType.DOUBLE, new DoubleTypeHandler());
        // string
        register(String.class, new StringTypeHandler());
        register(String.class, JdbcType.CHAR, new StringTypeHandler());
        register(String.class, JdbcType.VARCHAR, new StringTypeHandler());
        register(JdbcType.CHAR, new StringTypeHandler());
        register(JdbcType.VARCHAR, new StringTypeHandler());
        // Data
        register(Date.class, new DateTypeHandler());
        register(JdbcType.TIMESTAMP, new DateTypeHandler());
        // java.sql.Timestamp
        register(java.sql.Timestamp.class, new SqlTimestampTypeHandler());
        // unknown Object
        this.unknownTypeHandler = new UnknownTypeHandler(configuration);
        register(Object.class, unknownTypeHandler);
        register(Object.class, JdbcType.OTHER, unknownTypeHandler);
        register(JdbcType.OTHER, unknownTypeHandler);
    }

    public void register(JdbcType jdbcType, TypeHandler<?> handler) {
        jdbcTypeHandlerMap.put(jdbcType, handler);
    }

    public <T> TypeHandler<T> register(Class<T> javaType, TypeHandler<? extends T> typeHandler) {
        return this.register((Type) javaType, typeHandler);
    }

    private <T> TypeHandler<T> register(Type javaType, TypeHandler<? extends T> typeHandler) {
        // MappedJdbcTypes mappedJdbcType = typeHandler.getClass().getAnnotation(MappedJdbcTypes.class);
        if (true/*mappedJdbcType == null*/) {
            return (TypeHandler<T>) this.register(javaType, null, typeHandler);
        }
        /* MappedJdbcTypes 注解未完成
        for (JdbcType handledJdbcType : mappedJdbcType.value()) {
            register(javaType, handledJdbcType, typeHandler);
        }
        if (mappedJdbcType.includeNullJdbcType()) {
            register(javaType, null, typeHandler);
        }*/
        return null;
    }

    private TypeHandler<?> register(Type javaType, JdbcType jdbcType, TypeHandler<?> handler) {
        if (javaType != null) {
            Map<JdbcType, TypeHandler<?>> map = typeHandlerMap.get(javaType);
            if (map == null || map == NULL_TYPE_HANDLER_MAP) {
                map = new HashMap<>();
            }
            map.put(jdbcType, handler);
            typeHandlerMap.put(javaType, map);
        }
        return allTypeHandlersMap.put(handler.getClass(), handler);
    }

    public boolean hasTypeHandler(Class<?> javaType) {
        return hasTypeHandler(javaType, null);
    }

    public boolean hasTypeHandler(TypeReference<?> javaTypeReference) {
        return hasTypeHandler(javaTypeReference, null);
    }

    public boolean hasTypeHandler(Class<?> javaType, JdbcType jdbcType) {
        return javaType != null && getTypeHandler((Type) javaType, jdbcType) != null;
    }

    public boolean hasTypeHandler(TypeReference<?> javaTypeReference, JdbcType jdbcType) {
        return javaTypeReference != null && getTypeHandler(javaTypeReference, jdbcType) != null;
    }


    public TypeHandler<?> getMappingTypeHandler(Class<? extends TypeHandler<?>> handlerType) {
        return allTypeHandlersMap.get(handlerType);
    }


    public TypeHandler<?> getTypeHandler(JdbcType jdbcType) {
        return jdbcTypeHandlerMap.get(jdbcType);
    }


    public <T> TypeHandler<T> getTypeHandler(Class<T> type) {
        return getTypeHandler((Type) type, null);
    }

    public <T> TypeHandler<T> getTypeHandler(TypeReference<T> javaTypeReference) {
        return getTypeHandler(javaTypeReference, null);
    }

    public <T> TypeHandler<T> getTypeHandler(Class<T> type, JdbcType jdbcType) {
        return getTypeHandler((Type) type, jdbcType);
    }

    public <T> TypeHandler<T> getTypeHandler(TypeReference<T> javaTypeReference, JdbcType jdbcType) {
        return getTypeHandler(javaTypeReference.getRawType(), jdbcType);
    }


    private <T> TypeHandler<T> getTypeHandler(Type type, JdbcType jdbcType) {
        if (ParamNameResolver.ParamMap.class.equals(type)) {
            return null;
        }
        Map<JdbcType, TypeHandler<?>> jdbcHandlerMap = this.getJdbcHandlerMap(type);
        if (jdbcHandlerMap == null) {
            // type drives generics here
            return null;
        }
        TypeHandler<?> handler = jdbcHandlerMap.get(jdbcType);
        if (handler != null) {
            return (TypeHandler<T>) handler;
        }
        handler = jdbcHandlerMap.get(null);
        if (handler != null) {
            return (TypeHandler<T>) handler;
        }
        handler = pickSoleHandler(jdbcHandlerMap);
        return (TypeHandler<T>) handler;
    }

    /**
     * 如果type在{@link #typeHandlerMap}没有元素,
     * 向{@link #typeHandlerMap}里放入type为键, {@link #NULL_TYPE_HANDLER_MAP}为值,
     * 此时返回null.
     * 如果有值, 返回那个值
     *
     * @param type 对应的类没有TypeHandler, 就往父类去找, 如果是Enum, 就往父接口去找
     * @return {@link #typeHandlerMap}中依据Type获取Map, 没有就返回null
     */
    private Map<JdbcType, TypeHandler<?>> getJdbcHandlerMap(Type type) {
        Map<JdbcType, TypeHandler<?>> jdbcHandlerMap = typeHandlerMap.get(type);
        if (NULL_TYPE_HANDLER_MAP.equals(jdbcHandlerMap)) {
            return null;
        }
        if (jdbcHandlerMap == null && type instanceof Class) {
            Class<?> clazz = (Class<?>) type;
            if (!Enum.class.isAssignableFrom(clazz)) {
                jdbcHandlerMap = this.getJdbcHandlerMapForSuperclass(clazz);
            } else {
                // clazz是enum
                Class<?> enumClass = clazz.isAnonymousClass() ? clazz.getSuperclass() : clazz;
                jdbcHandlerMap = this.getJdbcHandlerMapForEnumInterfaces(enumClass, enumClass);
                if (jdbcHandlerMap == null) {
                    // defaultEnumTypeHandler实例化后实际上创建出的实例
                    // 是针对特定枚举类的TypeHandler
                    // 注册到typeHandlerMap
                    this.register(enumClass, this.getInstance(enumClass, defaultEnumTypeHandler));
                    return typeHandlerMap.get(enumClass);
                }
            }
        }
        // 更新jdbcHandlerMap
        typeHandlerMap.put(type, jdbcHandlerMap == null ? NULL_TYPE_HANDLER_MAP : jdbcHandlerMap);
        return jdbcHandlerMap;
    }


    /**
     * 获取 <b>父类</b> 的 TypeHandler Map<br>
     * 依据软件设计标准, 子类一定能存在于一切父类的位置<br>
     *
     * @return 向父类层层获取, 直到获取到TypeHandler, 如果没拿到就是null
     */
    private Map<JdbcType, TypeHandler<?>> getJdbcHandlerMapForSuperclass(Class<?> clazz) {
        Class<?> superclass = clazz.getSuperclass();
        if (superclass == null || Object.class.equals(superclass)) {
            // 没获取到
            return null;
        }
        Map<JdbcType, TypeHandler<?>> jdbcHandlerMap = typeHandlerMap.get(superclass);
        if (jdbcHandlerMap != null) {
            return jdbcHandlerMap;
        } else {
            return getJdbcHandlerMapForSuperclass(superclass);
        }
    }


    /**
     * @param enumClazz 具体枚举类型本身
     * @param clazz     具体枚举类父类的接口/本身, 不断向上寻找, 直到找到有TypeHandler的接口类型<br>
     *                  ({@link Enum}接口有{@link #defaultEnumTypeHandler})<br>
     *                  这些接口的所有TypeHandler汇聚成Map<JdbcType, TypeHandler<?>>返回
     * @return enumClass的所有父接口的Map<JdbcType, TypeHandler < ?>>汇总(没有, 就返回null)
     */
    private Map<JdbcType, TypeHandler<?>> getJdbcHandlerMapForEnumInterfaces(Class<?> clazz, Class<?> enumClazz) {
        for (Class<?> interfaceClass : clazz.getInterfaces()) {
            Map<JdbcType, TypeHandler<?>> jdbcHandlerMap = typeHandlerMap.get(interfaceClass);
            if (jdbcHandlerMap == null) {
                // 递归直到找到jdbcHandlerMap为止
                jdbcHandlerMap = getJdbcHandlerMapForEnumInterfaces(interfaceClass, enumClazz);
            }
            if (jdbcHandlerMap == null) {
                continue;
            }
            // Found a type handler registered to a super interface
            HashMap<JdbcType, TypeHandler<?>> newMap = new HashMap<>();
            for (Map.Entry<JdbcType, TypeHandler<?>> entry : jdbcHandlerMap.entrySet()) {
                // Create a type handler instance with enums type as a constructor arg
                newMap.put(entry.getKey(), getInstance(enumClazz, entry.getValue().getClass()));
            }
            return newMap;
        }
        return null;
    }

    /**
     * @param jdbcHandlerMap 要求获取的TypeHandler类型是相同的
     * @return 唯一类型的那种TypeHandler, 不唯一返回bull, jdbcHandlerMap空返回null
     */
    private TypeHandler<?> pickSoleHandler(Map<JdbcType, TypeHandler<?>> jdbcHandlerMap) {
        TypeHandler<?> soleHandler = null; // 最终被选举出的结果
        for (TypeHandler<?> handler : jdbcHandlerMap.values()) {
            if (soleHandler == null) {
                // 先让soleHandler有值
                soleHandler = handler;
                continue;
            }
            if (!handler.getClass().equals(soleHandler.getClass())) {
                // 超过一种TypeHandler被注册了
                // 包装soleHandler唯一
                return null;
            }
        }
        return soleHandler;
    }

    public Collection<TypeHandler<?>> getTypeHandlers() {
        return Collections.unmodifiableCollection(allTypeHandlersMap.values());
    }

    /**
     * 尝试用Class作为有参构造的参数实例化TypeHandler<br>
     * 不行, 就使用无参构造实例化TypeHandler
     */
    public <T> TypeHandler<T> getInstance(Class<?> javaTypeClass, Class<?> typeHandlerClass) {
        if (javaTypeClass != null) {
            // 有参构造
            try {
                Constructor<?> c = typeHandlerClass.getConstructor(Class.class);
                return (TypeHandler<T>) c.newInstance(javaTypeClass);
            } catch (NoSuchMethodException ignored) {
                // 有参构造失败
                // ignored
            } catch (Exception e) {
                throw new TypeException("Failed invoking constructor for handler " + typeHandlerClass, e);
            }
        }
        // 无参构造
        try {
            Constructor<?> c = typeHandlerClass.getConstructor();
            return (TypeHandler<T>) c.newInstance();
        } catch (Exception e) {
            throw new TypeException("Unable to find a usable constructor for " + typeHandlerClass, e);
        }
    }

}
