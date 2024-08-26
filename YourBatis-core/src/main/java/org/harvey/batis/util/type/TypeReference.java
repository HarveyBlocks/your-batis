package org.harvey.batis.util.type;

import lombok.Getter;
import org.harvey.batis.exception.type.TypeException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 获取
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-13 21:14
 * @see #rawType
 */
@Getter
public abstract class TypeReference<T> {
    /**
     * @see #getSuperclassTypeParameter(Class)
     */
    private final Type rawType;

    /**
     * @see #rawType
     */
    protected TypeReference() {
        rawType = this.getSuperclassTypeParameter(this.getClass());
    }

    /**
     * @param clazz 由于本类是抽象的类, 故找到其实一定是本类的子类
     * @return 本类子类的泛型列表的第一个泛型的运行时实际类型X, 然后忽略X的泛型列表
     */
    private Type getSuperclassTypeParameter(Class<?> clazz) {
        // 获取本实例对象的父类的带泛型类型
        Type genericSuperclass = clazz.getGenericSuperclass();
        if (!(genericSuperclass instanceof Class)) {
            // 递归出口
            // 父类有泛型参数列表
            // 获取泛型参数列表的第一个泛型类
            Type rawType = ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];

            // 如果获取的类型有泛型参数列表, 那么只获取类型本身而忽略泛型
            if (rawType instanceof ParameterizedType) {
                return ((ParameterizedType) rawType).getRawType();
            }
            return rawType;
        }
        // 如果父类是字节码类, 而不带泛型参数列表
        // 尝试向父类之父类尝试，直到遇到有价值的泛型参数列表
        if (TypeReference.class == genericSuperclass) {
            // 递归出口,
            // 出现了本TypeReference, 但是还是没有出现第泛型参数列表
            // 就抛出异常
            throw new TypeException("'" + getClass() + "' extends TypeReference but misses the type parameter. " + "Remove the extension or add a type parameter to it.");
        }
        // 递归
        return this.getSuperclassTypeParameter(clazz.getSuperclass());

    }

    /**
     * @return {@link #rawType}
     */
    @Override
    public String toString() {
        return rawType.toString();
    }
}
