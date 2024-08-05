package org.harvey.batis.reflection;

import java.lang.reflect.*;
import java.util.Arrays;

/**
 * 针对含有泛型参数列表的类, 本身就是泛型的类以及数组类的Type的调整封装
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-24 15:54
 */
public class TypeParameterResolver {

    private TypeParameterResolver() {
    }

    /**
     * 方法的返回值类型, 当返回值类型涉及泛型, 会经过处理之后返回在运行过程中的真正的类型
     *
     * @param srcType 当前所在的类, 当中不一定含有method, 因为method可能在srcType的父类中
     */
    public static Type resolveReturnType(Method method, Type srcType) {
        Type returnType = method.getGenericReturnType();// 返回类型同时包含泛型信息
        Class<?> declaringClass = method.getDeclaringClass();
        return resolveType(returnType, srcType, declaringClass);
    }

    /**
     * 对于返回值类型涉及泛型(是泛型, 含有泛型参数列表)或者是数组的情况特殊处理
     */
    private static Type resolveType(Type type, Type srcType, Class<?> declaringClass) {
        // 🤔 : 这里可以用责任链吧?有必要吗?
        if (type instanceof GenericArrayType) {
            // 数组类型
            return resolveGenericArrayType((GenericArrayType) type, srcType, declaringClass);
        }
        if (type instanceof TypeVariable) {
            // 泛型参数中的类型
            return resolveTypeVar((TypeVariable<?>) type, srcType, declaringClass);
        }
        if (type instanceof ParameterizedType) {
            // 带泛型参数的类型
            return resolveParameterizedType((ParameterizedType) type, srcType, declaringClass);
        }
        // 为何此处可以不考虑WildcardType类型?因为单独一个?不可能单独作为类型存在, 其出现一定是在泛型参数列表中的
        return type;
    }

    /**
     * 解析typeVar的泛型类型
     * 和scanSuperTypes一起构成递归, 扫描父类后得到运行时泛型类型, 一起构成完整的类型
     */
    private static Type resolveTypeVar(TypeVariable<?> typeVar, Type srcType, Class<?> declaringClass) {
        Type result;
        Class<?> clazz;
        if (srcType instanceof Class) {
            clazz = (Class<?>) srcType;
        } else if (srcType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) srcType;
            clazz = (Class<?>) parameterizedType.getRawType();
        } else {
            throw new IllegalArgumentException(
                    "The 2nd arg must be Class or ParameterizedType, but was: " + srcType.getClass());
        }

        if (clazz == declaringClass) {
            // 该方法就在该类中
            Type[] bounds = typeVar.getBounds();
            if (bounds.length > 0) {
                return bounds[0];
            }
            return Object.class;
        }
        // clazz != declaringClass
        // 该方法隐藏在该类的父类/父接口中
        Type superclass = clazz.getGenericSuperclass(); // 检查父类的泛型
        result = scanSuperTypes(typeVar, srcType, declaringClass, clazz, superclass);
        if (result != null) {
            return result;
        }

        Type[] superInterfaces = clazz.getGenericInterfaces(); // 检查父接口的泛型
        for (Type superInterface : superInterfaces) {
            result = scanSuperTypes(typeVar, srcType, declaringClass, clazz, superInterface);
            if (result != null) {
                return result;
            }
        }
        return Object.class;
    }


    /**
     * 扫描父类, 并分流(部分情况下递归)
     */
    private static Type scanSuperTypes(TypeVariable<?> typeVar, Type srcType,
                                       Class<?> declaringClass, Class<?> clazz,
                                       Type superclass) {
        if (superclass instanceof ParameterizedType) {
            // 父类的定义涉及到了泛型参数
            ParameterizedType parentAsType = (ParameterizedType) superclass;
            Class<?> parentAsClass = (Class<?>) parentAsType.getRawType(); // 去除泛型参数, 获取本类
            TypeVariable<?>[] parentTypeVars = parentAsClass.getTypeParameters(); // 泛型类型
            if (srcType instanceof ParameterizedType) {
                // 如果当前类也是涉及泛型参数
                parentAsType = translateParentTypeVars((ParameterizedType) srcType, clazz, parentAsType);
            }
            if (declaringClass == parentAsClass) {
                // 该方法就在该父类中
                for (int i = 0; i < parentTypeVars.length; i++) {
                    // 遍历父类的泛型参数列表
                    if (typeVar.equals(parentTypeVars[i])) {
                        // 该方法的返回值涉及的泛型参数在本父类中找到了, 是第i个
                        // 以此获取该父类的泛型参数列表中的第i个泛型类型
                        // 并返回
                        return parentAsType.getActualTypeArguments()[i];
                    }
                }
            }
            // 该方法在父类方法中
            if (declaringClass.isAssignableFrom(parentAsClass)) {
                // 方法所在类是当前类(superClass)或其父类
                // 在何时不满足? 未知.
                return resolveTypeVar(typeVar, parentAsType, declaringClass); // 递归检查
            }
        } else if (superclass instanceof Class && declaringClass.isAssignableFrom((Class<?>) superclass)) {
            // 条件二: 方法所在类是当前类(superClass)或其父类
            // 条件二在何时不满足? 未知.
            return resolveTypeVar(typeVar, superclass, declaringClass); // 递归检查
        }
        // 真的会走到这里来吗?
        return null;
    }


    /**
     * 检擦参数中的parentType
     * 如果其泛型列表中没有运行时变量带有泛型, 可以直接返回原来的值;
     * 否则, 将泛型列表中带泛型的变量重新赋值之后, 再封装后返回
     */
    private static ParameterizedType translateParentTypeVars(
            ParameterizedType srcType,
            Class<?> srcClass,
            ParameterizedType parentType) {
        Type[] parentTypeArgs = parentType.getActualTypeArguments(); // 父类的泛型参数(在运行时的类型)列表
        Type[] srcTypeArgs = srcType.getActualTypeArguments(); // 本类的泛型参数(在运行时的类型)列表
        TypeVariable<?>[] srcTypeVars = srcClass.getTypeParameters(); // srcType, 本类的泛型参数列表
        Type[] newParentArgs = new Type[parentTypeArgs.length];
        boolean noChange = true;
        for (int i = 0; i < parentTypeArgs.length; i++) {
            // 父类的泛型参数(在运行时的类型)列表中的第i个元素
            if (!(parentTypeArgs[i] instanceof TypeVariable)) {
                // 不再是泛型, 而是确定的类型
                newParentArgs[i] = parentTypeArgs[i];// 不变
                continue;
            }
            // 依旧是泛型
            // 需要到子类的泛型列表中寻找, 该泛型的真实类型是否被子类决定
            // 例如:
            // class Array<T> implements Comparable<T>{};, 父类Comparable的泛型有子类Array决定
            for (int j = 0; j < srcTypeVars.length; j++) {
                // 遍历子类(当前类)的泛型列表, 找到和当前遍历到的, 父类的泛型相同的泛型类型
                if (srcTypeVars[j].equals(parentTypeArgs[i])) { // 找到了
                    noChange = false;
                    newParentArgs[i] = srcTypeArgs[j]; // ? 既然是equals, 为什么选择了子类的泛型类型存入新数组
                    // 那么, 实质上这个位置上的的值, 无论赋值了子类的泛型参数列表上的值, 还是父类泛型参数列表上的值, 都是等价的
                    // 区别何在? 我想, 这个equals比较的是指向的地址是否相同, 而没有比较两个指针的类型是否相同
                    // parentTypeArgs[i]的类型是泛型参数类型, srcTypeVars[i]是具体参数类型, 即使指向了同一片空间
                    // 区别就在这儿
                    // 两个不同类型的指针指向了同一片内存空间,其编解码方式也不同
                    // 新(new)的区别就在于"编解码不同", 就是要保证编解码方式为"泛型参数类型"的不会出现
                }
            }
            if (noChange) {
                // 没有在子类中找到
                // 这个泛型的真实类型没有被子类确定, 真不知道这个泛型的类型是啥
                // 则不给这个地方赋值, 则是不正常的
                throw new IllegalStateException();
            }
        }

        return noChange ? parentType  // 父类的泛型参数列表中没有运行时变量带有泛型, 可以直接返回
                : new SpecialTypeImpl.ParameterizedTypeImpl((Class<?>) parentType.getRawType(), null, newParentArgs);
    }

    /**
     * 解析含有泛型参数的类
     */
    private static ParameterizedType resolveParameterizedType(ParameterizedType parameterizedType,
                                                              Type srcType,
                                                              Class<?> declaringClass) {
        Class<?> rawType = (Class<?>) parameterizedType.getRawType();
        Type[] typeArgs = parameterizedType.getActualTypeArguments();
        // 通配符类型(WildcardType)
        Type[] args = resolveWildcardTypeBounds(typeArgs, srcType, declaringClass);
        return new SpecialTypeImpl.ParameterizedTypeImpl(rawType, null, args);
    }


    /**
     * 考虑并处理存在在泛型参数列表中的通配符类型
     * 通配符类型本身就是个?, 没有任何价值和意义
     * 但因其可能有extends和super限制
     * extends和super限制成为其唯一携带的信息
     */
    private static Type[] resolveWildcardTypeBounds(Type[] bounds, Type srcType, Class<?> declaringClass) {
        Type[] result = new Type[bounds.length];
        for (int i = 0; i < bounds.length; i++) {
            if (bounds[i] instanceof TypeVariable) {
                result[i] = resolveTypeVar((TypeVariable<?>) bounds[i], srcType, declaringClass);
            } else if (bounds[i] instanceof ParameterizedType) {
                result[i] = resolveParameterizedType((ParameterizedType) bounds[i], srcType, declaringClass);
            } else if (bounds[i] instanceof WildcardType) {
                // 为何在这里突然开始考虑WildcardType, 而不再考虑GenericArrayType了?
                // 难道GenericArrayType不可能作为泛型参数出现吗?
                // 对: class MyClass<T[]>{}; 就是不可以的.
                result[i] = resolveWildcardType((WildcardType) bounds[i], srcType, declaringClass);
            } else {
                result[i] = bounds[i];
            }
        }

        return result;
    }

    /**
     * 递归, 保证WildcardType的Bounds中不会存在JDK原生的WildcardType,
     * 而是全部转换成自定义的WildcardTypeImpl封装
     */
    private static Type resolveWildcardType(WildcardType wildcardType, Type srcType, Class<?> declaringClass) {
        Type[] lowerBounds = resolveWildcardTypeBounds(wildcardType.getLowerBounds(), srcType, declaringClass);
        Type[] upperBounds = resolveWildcardTypeBounds(wildcardType.getUpperBounds(), srcType, declaringClass);
        return new SpecialTypeImpl.WildcardTypeImpl(lowerBounds, upperBounds);
    }

    /**
     * 解析数组类, 特别的: 泛型类数组, 含有泛型参数列表的类的数组
     */
    private static Type resolveGenericArrayType(GenericArrayType genericArrayType, Type srcType, Class<?> declaringClass) {
        Type componentType = genericArrayType.getGenericComponentType();
        Type resolvedComponentType = resolveType(componentType, srcType, declaringClass);
        if (/*resolvedComponentType != componentType && */resolvedComponentType instanceof Class) {
            return Array.newInstance((Class<?>) resolvedComponentType, 0).getClass();
        } else {
            // componentType不是Class, 不是GenericArrayType,不是TypeVariable,不是ParameterizedType, 放下不提
            // 🤔 : componentType是Class,但是由于源码的奇怪逻辑而要进入本分支, 十分奇怪,不能直接转为Class吗?难道一定要等到外面才进一步转成Class吗?
            return new SpecialTypeImpl.GenericArrayTypeImpl(resolvedComponentType);
        }
        // 下面是源码的逻辑
        /*
        Type resolvedComponentType = null;
        if (componentType instanceof TypeVariable) {
            resolvedComponentType = ...;
        } else if (componentType instanceof GenericArrayType) {
            resolvedComponentType = ...;
        } else if (componentType instanceof ParameterizedType) {
            resolvedComponentType = ...;
        } else{
            resolvedComponentType = null;
        }
        if (resolvedComponentType instanceof Class) { // null instanceof Class == false
            return Array.newInstance((Class<?>) resolvedComponentType, 0).getClass();
        } else {
            return new GenericArrayTypeImpl(resolvedComponentType);
        }
        */
    }

    /**
     * 解析方法的参数的运行时(考虑泛型等)类型<br/>
     * 特别的, 该方法针对只有一个参数的方法
     */
    public static Type resolveParamType(Method method, Class<?> srcType) {
        Type[] types = TypeParameterResolver.resolveParamTypes(method, srcType);
        if (types.length == 1) {
            return types[0];
        }
        throw new IllegalStateException("You should use method: " +
                "TypeParameterResolver#resolveParamTypes(Method, Type)");
    }

    /**
     * 获取参数列表的类型数组后, 遍历数组, 每一个的解析方式同解析返回值的方式
     */
    public static Type[] resolveParamTypes(Method method, Type srcType) {
        Type[] paramTypes = method.getGenericParameterTypes();
        Class<?> declaringClass = method.getDeclaringClass();
        Type[] result = new Type[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            result[i] = resolveType(paramTypes[i], srcType, declaringClass);
        }
        return result;
    }

    /**
     * 字段的类型解析方法和原理同解析返回值的方式
     */
    public static Type resolveFieldType(Field field, Class<?> srcType) {
        Type fieldType = field.getGenericType();
        Class<?> declaringClass = field.getDeclaringClass();
        return resolveType(fieldType, srcType, declaringClass);
    }

}

class SpecialTypeImpl {
    /**
     * 对ParameterizedType类型的封装
     */
    static class ParameterizedTypeImpl implements ParameterizedType {
        private final Class<?> rawType;

        private final Type ownerType;

        private final Type[] actualTypeArguments;


        public ParameterizedTypeImpl(Class<?> rawType, Type ownerType, Type[] actualTypeArguments) {
            this.rawType = rawType;
            this.ownerType = ownerType;
            this.actualTypeArguments = actualTypeArguments;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return actualTypeArguments;
        }

        @Override
        public Type getOwnerType() {
            return ownerType;
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public String toString() {
            return "ParameterizedTypeImpl [rawType=" + rawType +
                    ", ownerType=" + ownerType +
                    ", actualTypeArguments=" +
                    Arrays.toString(actualTypeArguments) + "]";
        }
    }

    static class WildcardTypeImpl implements WildcardType {
        private final Type[] lowerBounds;

        private final Type[] upperBounds;

        WildcardTypeImpl(Type[] lowerBounds, Type[] upperBounds) {
            this.lowerBounds = lowerBounds;
            this.upperBounds = upperBounds;
        }

        @Override
        public Type[] getLowerBounds() {
            return lowerBounds;
        }

        @Override
        public Type[] getUpperBounds() {
            return upperBounds;
        }
    }

    static class GenericArrayTypeImpl implements GenericArrayType {
        private final Type genericComponentType;

        GenericArrayTypeImpl(Type genericComponentType) {
            this.genericComponentType = genericComponentType;
        }

        @Override
        public Type getGenericComponentType() {
            return genericComponentType;
        }
    }
}