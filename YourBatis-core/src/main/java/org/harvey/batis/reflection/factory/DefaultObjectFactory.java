package org.harvey.batis.reflection.factory;

import org.harvey.batis.exception.reflection.ReflectionException;
import org.harvey.batis.reflection.Reflector;
import org.harvey.batis.util.function.ThrowableFunction;
import org.harvey.batis.util.function.ThrowableSupplier;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 默认ObjectFactory
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-24 13:18
 */
public class DefaultObjectFactory implements ObjectFactory, Serializable {
    // 🤔 : 为什么是Serializable

    private static final Map<Class<?>, Class<?>> COLLECTION_INTERFACE_IMPL_MAP = new HashMap<>();

    static {
        COLLECTION_INTERFACE_IMPL_MAP.put(List.class, ArrayList.class);
        COLLECTION_INTERFACE_IMPL_MAP.put(Collection.class, ArrayList.class);
        COLLECTION_INTERFACE_IMPL_MAP.put(Iterable.class, ArrayList.class);
        COLLECTION_INTERFACE_IMPL_MAP.put(Map.class, HashMap.class);
        COLLECTION_INTERFACE_IMPL_MAP.put(SortedSet.class, TreeSet.class);
        COLLECTION_INTERFACE_IMPL_MAP.put(Set.class, HashSet.class);
    }

    @Override
    public <T> T create(Class<T> type) {
        return this.create(type, null, null);
    }

    @Override
    public <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
        Class<?> classToCreate = resolveInterface(type);
        return (T) instantiateClass(classToCreate, constructorArgTypes, constructorArgs);
    }

    @Override
    public <T> boolean isCollection(Class<T> type) {
        return Collection.class.isAssignableFrom(type);
    }
    /**
     * 处理type是集合接口类型的情况
     * 由于接口不同实例化, 故选择其实现类实例化
     */
    private Class<?> resolveInterface(Class<?> type) {
        // return resolveInterface0(type);
        return COLLECTION_INTERFACE_IMPL_MAP.getOrDefault(type, type);
    }

    /**
     * @deprecated
     */
    @Deprecated
    private static Class<?> resolveInterface0(Class<?> type) {
        Class<?> classToCreate;
        if (type == List.class || type == Collection.class || type == Iterable.class) {
            classToCreate = ArrayList.class;
        } else if (type == Map.class) {
            classToCreate = HashMap.class;
        } else if (type == SortedSet.class) { // issue #510 Collections Support
            classToCreate = TreeSet.class;
        } else if (type == Set.class) {
            classToCreate = HashSet.class;
        } else {
            classToCreate = type;
        }
        return classToCreate;
    }

    private <T> T instantiateClass(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
        try {
            T newInstance;
            if (constructorArgTypes == null || constructorArgs == null) {
                // 无参构造
                newInstance = instantiate(
                        type::getDeclaredConstructor,
                        Constructor::newInstance
                );
            } else {
                // else: 有参构造
                newInstance = instantiate(
                        () -> type.getDeclaredConstructor(constructorArgTypes.toArray(new Class[0])),
                        constructor -> constructor.newInstance(constructorArgs.toArray(new Object[0]))
                );
            }
            return newInstance;
        } catch (Exception e) {
            // 以好看的形式记录异常栈信息
            String argTypes = list2String(constructorArgTypes, Class::getSimpleName);
            String argValues = list2String(constructorArgs, String::valueOf);
            throw new ReflectionException(
                    "Error instantiating " + type +
                            " with invalid types (" + argTypes + ") or " +
                            "values (" + argValues + "). Cause: " + e, e);
        }
    }

    private static <T> String list2String(List<T> constructorArgs, Function<T, String> mapper) {
        return Optional.ofNullable(constructorArgs)
                .orElseGet(Collections::emptyList)
                .stream().map(mapper)
                .collect(Collectors.joining(","));
    }

    private static <T> T instantiate(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs)
            throws NoSuchMethodException, InstantiationException, InvocationTargetException, IllegalAccessException {
        Constructor<T> constructor;
        constructor = type.getDeclaredConstructor(constructorArgTypes.toArray(new Class[0]));
        try {
            return constructor.newInstance(constructorArgs.toArray(new Object[0]));
        } catch (IllegalAccessException e) {
            if (Reflector.canControlMemberAccessible()) {
                constructor.setAccessible(true);
                return constructor.newInstance(constructorArgs.toArray(new Object[0]));
            } else {
                throw e;
            }
        }
    }

    private static <T> T instantiate(
            ThrowableSupplier<Constructor<T>, ReflectiveOperationException> declaredConstructorGetter,
            ThrowableFunction<Constructor<T>, T, ReflectiveOperationException> newInstanceCreator) throws ReflectiveOperationException {
        Constructor<T> constructor;
        constructor = declaredConstructorGetter.get();
        try {
            return newInstanceCreator.apply(constructor);
        } catch (IllegalAccessException e) {
            if (Reflector.canControlMemberAccessible()) {
                constructor.setAccessible(true);
                return newInstanceCreator.apply(constructor);
            } else {
                throw e;
            }
        }
    }
}



