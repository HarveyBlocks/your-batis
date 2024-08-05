package org.harvey.batis.reflection;

import org.harvey.batis.reflection.invoke.Invoker;
import org.harvey.batis.reflection.invoke.MethodInvoker;
import org.harvey.batis.reflection.invoke.ReadableFieldInvoker;
import org.harvey.batis.reflection.property.PropertyTokenizer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 反射工具类, 通过反射, 解析Getter和Setter, 获取方法对象, 字段对象等
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-25 13:34
 */
public class MetaClass {
    private final ReflectorFactory reflectorFactory;
    private final Reflector reflector;

    private MetaClass(Class<?> type, ReflectorFactory reflectorFactory) {
        this.reflectorFactory = reflectorFactory;
        this.reflector = reflectorFactory.findForClass(type);
    }

    /**
     * 对MetaClass的构造器的一个封装
     *
     * @param type 对象的字节码对象
     * @return 对应的MetaClass对象
     */
    public static MetaClass forClass(Class<?> type, ReflectorFactory reflectorFactory) {
        return new MetaClass(type, reflectorFactory);
    }

    /**
     * @see Reflector#getGetInvoker(String)
     */
    public Invoker getGetInvoker(String name) {
        return reflector.getGetInvoker(name);
    }

    /**
     * @see Reflector#getSetInvoker(String)
     */
    public Invoker getSetInvoker(String name) {
        return reflector.getSetInvoker(name);
    }

    /**
     * @param useCamelCaseMapping 使用 Camel/驼峰 形式的 Mapping
     * @return 将原生的name进行解析并转化为符合类型实际的, 连第一级配置都不存在则返回null
     */
    public String findProperty(String name, boolean useCamelCaseMapping) {
        if (useCamelCaseMapping) {
            // 因为配置名是忽略大小写存储的
            name = name.replace("_", "");
        }
        return this.findProperty(name);
    }

    /**
     * <br/>将原生态的配置字符串映射成真实配置字符串,
     * <br/>配置之间调用的过程中可能存在配置不存在的情况,
     * <br/>如果配置不存在, 就直接在上一级中断
     *
     * @param name 例如 school.students[12].score.age
     * @return score不会存在age字段, 则返回school.students[12].score
     */
    private String findProperty(String name) {
        StringBuilder prop = this.buildProperty(name, new StringBuilder());
        return prop.length() > 0 ? prop.toString() : null;
    }


    /**
     * @param name    原生态的配置名, 可能存在不存在的配置, 如果配置不存在, 就直接在上一级中断<br>
     *                判断配置存在与否是忽略大小写的, 返回的配置字符串的形式是根据解析Getter/Setter来的
     * @param builder 作为递归过程中传递的值, 是需要被构建的最终结果
     */
    private StringBuilder buildProperty(String name, StringBuilder builder) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        // 这种看见好多了, 明明可以用while(hasNext){next}的形式,
        // 它非得用递归.., 咋地, 函数调用效率就这么高?
        if (prop.hasNext()) {
            String propertyName = reflector.findPropertyName(prop.getName());
            if (propertyName != null) {
                builder.append(propertyName).append(".");
                // 新的property形成的新的metaClass,也形成了一个递归
                MetaClass metaProp = this.metaClassForProperty(propertyName);
                metaProp.buildProperty(prop.getChildrenFullname(), builder);
                // 由于是对builder参数写入式的,所以, 忽略返回值也罢
            }
        } else {
            String propertyName = reflector.findPropertyName(name);
            if (propertyName != null) {
                builder.append(propertyName);
                // 不加点"."了, 递归出口
            }
        }
        return builder;
    }

    /**
     * @param name 当前需要获取的配置的配置名
     * @return 从当前对象的Getter中获取返回值的类型对象
     */
    private MetaClass metaClassForProperty(String name) {
        Class<?> propType = reflector.getGetterType(name);
        return MetaClass.forClass(propType, reflectorFactory);
    }


    /**
     * @param prop 如果要依据PropertyTokenizer来获取接下来的get类型, 则需要考虑该依赖配置是Collection的情况
     * @return 从当前对象的Getter中获取返回值的类型对象
     */
    private MetaClass metaClassForProperty(PropertyTokenizer prop) {
        Class<?> propType = getGetterType(prop);
        return MetaClass.forClass(propType, reflectorFactory);
    }

    /**
     * @see Reflector#getReadablePropertyNames()
     */
    public String[] getGetterNames() {
        return reflector.getReadablePropertyNames();
    }

    /**
     * @see Reflector#getWritablePropertyNames()
     */
    public String[] getSetterNames() {
        return reflector.getWritablePropertyNames();
    }

    /**
     * school.student[12].name, 利用递归直接获取name的类型
     *
     * @return 返回配置的最终底层依赖的类对象
     */
    public Class<?> getSetterType(String fullname) {
        PropertyTokenizer prop = new PropertyTokenizer(fullname);
        if (!prop.hasNext()) {
            // 递归出口, 返回配置的最终底层依赖的类对象
            // 🤔 : 如果Setter的类型是集合,
            //      那么本函数返回的类型是集合的类型合理,
            //      还是集合元素的类型合理?
            // 答 : 由于是要往字段里写入, 如果忽略了集合的类型, 那么写入的方式必将收到影响
            //      使用反射的人, 是想要直到集合的类型的, 这样方能准确写入
            return reflector.getSetterType(prop.getName());
        }
        // 依据配置名获取当前对象的依赖, 然后获取该依赖的类对象
        MetaClass metaProp = metaClassForProperty(prop.getName());
        // 依据依赖的类对象, 从中Setter获取下一个配置
        // 构成递归
        return metaProp.getSetterType(prop.getChildrenFullname());
        // 🤔 : 为什么要先用Getter获取当前依赖的类对象, 再用Setter获取该依赖的子依赖的类对象
        //      Setter和Getter的选择有什么依据?
    }

    /**
     * 依据Getter方法获取元素类型, 如果Getter返回类型是集合, 则返回集合中的元素;<br>
     * 如果Getter的返回类型是{@code Class<Integer>}, 就不会返回{@code Class<Class<Integer>>}, 而是返回{@code Class<Integer>}
     */
    public Class<?> getGetterType(String fullname) {
        PropertyTokenizer prop = new PropertyTokenizer(fullname);
        if (!prop.hasNext()) {
            // 递归出口
            // issue #506. Resolve the type inside a Collection Object
            // 解析 Collection 对象内的元素类型, 并返回
            return getGetterType(prop);
        }
        MetaClass metaProp = metaClassForProperty(prop);
        return metaProp.getGetterType(prop.getChildrenFullname());

    }

    /**
     * 解析 Collection 对象内元素的类型
     *
     * @return 如果依赖是集合, 则进行解析, 不是则返回(数组类型也返回)
     * 解析处集合的元素类型(依据泛型), 返回集合中元素的类型
     */
    private Class<?> getGetterType(PropertyTokenizer prop) {
        // 先简单地依据配置名获取Getter类型的对应的类型
        Class<?> type = reflector.getGetterType(prop.getName());
        if (prop.getIndex() == null || !Collection.class.isAssignableFrom(type)) {
            // 配置名解析后, 当前配置不含有索引
            // 或不含有index(此情况为配置名和配置不一致), 然后配置的类型不继承自集合
            return type;
        }
        // 目标配置是自集合, 且要求获取其第index个对象

        // 解析了泛型之后返回类型
        Type returnType = getGenericGetterType(prop.getName());
        if (!(returnType instanceof ParameterizedType)) {
            // 目标配置不含有泛型参数列表的数组类型, 可以直接返回
            // 对于集合来说, 这里是没有泛型的集合, 或者Map之类
            return type;
        }
        Type[] actualTypeArguments = ((ParameterizedType) returnType).getActualTypeArguments();
        if (actualTypeArguments == null || actualTypeArguments.length != 1) {
            // 泛型类型列表的长度不为1, 可以直接返回
            return type;
        }
        // Getter的返回值类型转变为了集合的元素的类型
        returnType = actualTypeArguments[0];
        if (returnType instanceof Class) {
            // 当集合的元素是Class<?>时, 返回的类型不是Class<Class<?>>, 而是Class<?>, 就挺奇妙的
            type = (Class<?>) returnType; // 如果集合元素是字节码对象的集合, 转换成字节码对象
        } else if (returnType instanceof ParameterizedType) {
            // 如果集合元素是依旧有泛型的类型, 且不是字节码对象
            // 直接忽略集合元素中的泛型, 例如List<Set<String>>, 剥去List后有元素为Set<String>, 直接返回Set类型
            type = (Class<?>) ((ParameterizedType) returnType).getRawType();
        }
        return type;
    }

    /**
     * 依据配置名找到对应Getter或Field, 从当前对象中获取配置, 并解析其泛型后返回
     */
    private Type getGenericGetterType(String propertyName) {
        Invoker invoker = reflector.getGetInvoker(propertyName);
        if (invoker instanceof MethodInvoker) {
            /* 🤔 : 为什么要使用反射? 不是可以再MethodInvoker里面添加get方法来获取字段的吗?
             Field declaredMethod = MethodInvoker.class.getDeclaredField("method");
             declaredMethod.setAccessible(true);
             Method method = (Method) declaredMethod.get(invoker);*/
            // 是为了改变类型转换? 但是...
            Method method = ((MethodInvoker) invoker).getMethod();
            return TypeParameterResolver.resolveReturnType(method, reflector.getType());
        } else if (invoker instanceof ReadableFieldInvoker) {
                /* 🤔 : 同上
                Field declaredField = AbstractFieldInvoker.class.getDeclaredField("field");
                declaredField.setAccessible(true);
                Field field = (Field) declaredField.get(invoker);*/
            Field field = ((ReadableFieldInvoker) invoker).getField();
            return TypeParameterResolver.resolveFieldType(field, reflector.getType());
        }
        /*catch (NoSuchFieldException | IllegalAccessException e) {
            // Ignored
        }*/
        return null;
    }


    /**
     * 解析配置, 然后返回最终配置是否存在
     *
     * @return true 表示存在
     */
    public boolean hasSetter(String name) {
        return this.hasMethod(name, reflector::hasSetter, MetaClass::hasSetter);
    }

    public boolean hasGetter(String name) {
        return this.hasMethod(name, reflector::hasGetter, MetaClass::hasGetter);
    }

    @FunctionalInterface
    private interface ReflectorHasMethodFunction extends Function<String, Boolean> {
    }

    @FunctionalInterface
    private interface MetaPropHasMethodBiFunction extends BiFunction<MetaClass, String, Boolean> {
    }


    private boolean hasMethod(String name,
                              ReflectorHasMethodFunction reflectorHasMethod,
                              MetaPropHasMethodBiFunction metaPropHasMethod) {
        // 假设student.score.math, 当前是Student的MetaClass
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (!prop.hasNext()) {
            // 递归出口
            return reflectorHasMethod.apply(prop.getName());
        }
        if (!reflectorHasMethod.apply(prop.getName())) {
            return false;
        }
        // 获取score的MetaClass
        MetaClass metaProp = metaClassForProperty(prop.getName());
        // 构成递归, 找寻math在score中是否存在
        return metaPropHasMethod.apply(metaProp, prop.getChildrenFullname());
    }
}