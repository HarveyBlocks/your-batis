package org.harvey.batis.reflection;

import lombok.Getter;
import org.harvey.batis.exception.reflection.ReflectionException;
import org.harvey.batis.reflection.invoke.*;
import org.harvey.batis.reflection.property.FieldProperties;

import java.lang.reflect.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Stream;

/**
 * 一组缓存的类定义信息，允许在属性名称和 getter-setter 方法之间轻松映射。
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-24 13:39
 */
public class Reflector {

    /**
     * 映射指向的对象类本体
     */
    @Getter
    private final Class<?> type;
    /**
     * 有get方法的字段
     */
    @Getter
    private final String[] readablePropertyNames;
    /**
     * 有set方法的字段
     */
    @Getter
    private final String[] writablePropertyNames;
    private final Map<String, Invoker> setMethods = new HashMap<>();
    private final Map<String, Invoker> getMethods = new HashMap<>();
    private final Map<String, Class<?>> setTypes = new HashMap<>();
    private final Map<String, Class<?>> getTypes = new HashMap<>();
    private Constructor<?> defaultConstructor;

    /**
     * 不区分大小写的属性映射, 键是被全部大写了的属性名, 值是属性名
     */
    private final Map<String, String> caseInsensitivePropertyMap = new HashMap<>();

    public Reflector(Class<?> clazz) {
        type = clazz;
        addDefaultConstructor(clazz);

        Method[] methods = getClassMethods(clazz);
        addGetMethods(Arrays.stream(methods).filter(FieldProperties::isGetter));
        addSetMethods(Arrays.stream(methods).filter(FieldProperties::isSetter));

        addFields(clazz);

        readablePropertyNames = analyzePropertyName(getMethods);
        writablePropertyNames = analyzePropertyName(setMethods);
    }

    /**
     * @return 在系统层面是否开放对default、protected以及private字段的访问权限
     */
    public static boolean canControlMemberAccessible() {
        try {
            SecurityManager securityManager = System.getSecurityManager();
            if (null != securityManager) {
                // suppressAccessChecks
                // 屏蔽java原本对字段和方法的各种访问权限校验
                // 不仅可以访问公共成员，还能访问default、protected以及private成员。
                securityManager.checkPermission(new ReflectPermission("suppressAccessChecks"));
            }
        } catch (SecurityException e) {
            return false;
        }
        return true;
    }


    private void addDefaultConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        Arrays.stream(constructors)
                // 找出其中的无参构造
                .filter(constructor -> constructor.getParameterTypes().length == 0)
                // 从诸多构造器中选出一个
                .findAny()
                // 存在
                .ifPresent(constructor -> this.defaultConstructor = constructor);
    }


    private void addGetMethods(Stream<Method> getterStream) {
        // conflictingGetters即除去了部分不和规范的字段名之后仍保留下来的方法
        // 方法名-方法映射, 考虑到存在同名函数但不同方法签名的情况
        Map<String, List<Method>> conflictingGetters = new HashMap<>();
        getterStream.forEach(m -> addMethodConflict(conflictingGetters, FieldProperties.methodToProperty(m.getName()), m));
        resolveGetterConflicts(conflictingGetters);
    }

    /**
     * 返回包含在此类和任何父类中声明的所有方法。
     * 也包含私有方法。
     * 且保证方法的签名的唯一性
     */
    private Method[] getClassMethods(Class<?> clazz) {
        // 由于父类的方法到子类可能存在重载的现象, 做一个去重, 故称之为unique
        Map<String, Method> uniqueMethods = new HashMap<>();
        Class<?> currentClass = clazz; // 逐层向父类找寻方法
        while (currentClass != null && currentClass != Object.class) {
            addUniqueMethods(uniqueMethods, currentClass.getDeclaredMethods());

            // 当类是抽象类的时候, 也要检查接口方法
            Class<?>[] interfaces = currentClass.getInterfaces();
            for (Class<?> anInterface : interfaces) {
                addUniqueMethods(uniqueMethods, anInterface.getMethods());
            }

            currentClass = currentClass.getSuperclass();
        }

        Collection<Method> methods = uniqueMethods.values();
        return methods.toArray(new Method[0]);
    }

    /**
     * 将methods加入uniqueMethods中, 并通过方法签名保证方法的唯一性
     */
    private void addUniqueMethods(Map<String, Method> uniqueMethods, Method[] methods) {
        for (Method currentMethod : methods) {
            if (!currentMethod.isBridge()) {
                String signature = getSignature(currentMethod);
                // 检查该方法是否是已知的方法
                if (!uniqueMethods.containsKey(signature)) {
                    uniqueMethods.put(signature, currentMethod);
                }
                // 如果是已知的方法, 则表示其子类已经重载了该方法
            }
        }
    }

    /**
     * 获取方法的签名
     * 方法的签名限定了方法的唯一性(同名方法的重写)
     */
    private String getSignature(Method method) {
        // int exec(double a,double b){};
        // Method->String
        // int#exec:double,double
        StringBuilder sb = new StringBuilder();
        Class<?> returnType = method.getReturnType();
        // 返回值void也有name, 曰: "void"
        sb.append(returnType.getName()).append('#');
        sb.append(method.getName());
        Class<?>[] parameters = method.getParameterTypes();
        for (int i = 0; i < parameters.length; i++) {
            sb.append(i == 0 ? ':' : ',').append(parameters[i].getName());
        }

        return sb.toString();
    }

    /**
     * 去除一些不符合property名规则的方法
     * 同property的方法会被加入到
     */
    private void addMethodConflict(Map<String, List<Method>> conflictingMethods, String name, Method method) {
        if (isValidPropertyName(name)) {
            List<Method> list = conflictingMethods
                    // 如果不存在，则对值进行操作
                    // 如果存在, 则取出这个值
                    .computeIfAbsent(name, k -> new ArrayList<>());// 返回操作后的值
            list.add(method);
        }
    }

    /**
     * @return 是有效的属性名称
     */
    private boolean isValidPropertyName(String name) {
        return !name.startsWith("$") && // 字段不能以$打头, 这种可能是代理类之类的了
                !"serialVersionUID".equals(name) && // serialVersionUID将不列入考虑
                !"class".equals(name); // 一个方法叫getClass不代表其有一个叫做class的字段

    }


    /**
     * 多个Getter方法, 由于继承, 重载的原因可能存在逻辑和签名的不同<br/>
     * 本方法通过对比返回值, 来判断Getter是否处于一种"摸棱两可"的状态<br/>
     * 所谓摸棱两可, 就是子类和父类中的同名Getter方法有不同类型的返回值, 且返回值之间不存在父子关系<br/>
     * 对于多种不同的情况(摸棱两可的, 模棱两可中也有其他情况, 不摸棱两可的), 给出不同的Getter方法的代理<br/>
     * 并将该方法存储起来
     *
     * @param conflictingGetters 存放了所有(私有的, 父类中的)Getter方法
     */
    private void resolveGetterConflicts(Map<String, List<Method>> conflictingGetters) {
        for (Map.Entry<String, List<Method>> entry : conflictingGetters.entrySet()) {
            // 选举
            Method winner = null;
            String propName = entry.getKey();
            boolean isAmbiguous = false; // 模棱两可的
            for (Method candidate : entry.getValue()) {
                // 候选人 candidate
                if (winner == null) {
                    winner = candidate; // 为winner赋初值
                    continue;
                }
                Class<?> winnerReturnType = winner.getReturnType();
                Class<?> candidateType = candidate.getReturnType();
                if (candidateType.equals(winnerReturnType)) {
                    if (!boolean.class.equals(candidateType)) {
                        // 候选人和选举着的返回值是相同的
                        // 什么鸟情况? 函数名相同的情况下, 在Getter中, 参数列表都是空, 而且经过了去重
                        // 返回值类型即成为函数之间的唯一区别
                        // 而还是存在返回值类型相同的情况,
                        // 这不就说明前面的去重没去清楚, 或者还保留了参数列表非空的方法吗?
                        isAmbiguous = true;
                        break;
                    } else if (candidate.getName().startsWith("is")) {
                        winner = candidate;
                    }
                } /*else if (candidateType.isAssignableFrom(winnerReturnType)) {
                     A.isAssignableFrom(B)
                     A是B的父类
                     或接口A是接口B的父接口
                     或接口A是类B的接口
                     或A, B两个类相同

                     候选人的返回值是Winner父类, Winner不变
                } */ else if (winnerReturnType.isAssignableFrom(candidateType)) {
                    // 候选人的返回值是Winner子类, Winner不变
                    winner = candidate;
                } else {
                    // 候选人和winner的返回值类型毫无关系, 不知道写这个方法的时候是怎么想的
                    // 一般这种都是不能确定逻辑, 需要报错的吧?
                    isAmbiguous = true;
                    break;
                }
            }
            addGetMethod(propName, winner, isAmbiguous);
        }
    }


    /**
     * 将不同的方法进行不同的封装, 然后存储该封装
     *
     * @param name        方法名
     * @param method      Getter
     * @param isAmbiguous 模棱两可的
     */
    private void addGetMethod(String name, Method method, boolean isAmbiguous) {
        MethodInvoker invoker = isAmbiguous ?
                new AmbiguousMethodInvoker(method, MessageFormat.format(
                        "Illegal overloaded getter method with " +
                                "ambiguous type for property ''{0}'' in class ''{1}''. " +
                                "This breaks the JavaBeans specification and can cause unpredictable results.",
                        name, method.getDeclaringClass().getName())) :
                new MethodInvoker(method);
        getMethods.put(name, invoker);
        Type returnType = TypeParameterResolver.resolveReturnType(method, type);
        getTypes.put(name, typeToClass(returnType));
    }

    /**
     * Type中包含有泛型信息, 这是Class类型不能体现的, <br/>
     * 所以对于涉及泛型的不同类型进行到Class的转换
     */
    private Class<?> typeToClass(Type src) {
        Class<?> result;
        if (src instanceof Class) {
            result = (Class<?>) src;
        } else if (src instanceof ParameterizedType) {
            // 带有泛型的类型为ParameterizedType
            // 获取类本身. List<String>, 就获取类型List
            result = (Class<?>) ((ParameterizedType) src).getRawType();
        } else if (src instanceof GenericArrayType) {
            // GenericArrayType 表示一个数组类型
            Type componentType = ((GenericArrayType) src).getGenericComponentType();
            Class<?> componentClass = typeToClass(componentType); // 递归获取类型
            // 构建数组, 获取数组的Class类型
            result = Array.newInstance(componentClass, 0).getClass();
        } else {
            result = Object.class;
        }
        return result;
    }

    /**
     * @param setterStream 其中的Setter并解析, 包装之后存入字段setMethods
     */
    private void addSetMethods(Stream<Method> setterStream) {
        Map<String, List<Method>> conflictingSetters = new HashMap<>();
        setterStream.forEach(m -> addMethodConflict(conflictingSetters, FieldProperties.methodToProperty(m.getName()), m));
        resolveSetterConflicts(conflictingSetters);
    }

    /**
     * 通过解析每一个字段, 及其对应的各个Setter, 比对该配置对应的Getter, 找出最合适的Setter并存储
     */
    private void resolveSetterConflicts(Map<String, List<Method>> conflictingSetters) {
        for (Map.Entry<String, List<Method>> entry : conflictingSetters.entrySet()) {
            String propName = entry.getKey();
            List<Method> setters = entry.getValue();
            // Setter的类型要求和Getter的类型对应
            // 考虑到该方法只可写不可读, 没有对应Getter方法? 这种情况少见吧?
            // 是故先解析Getter, 再解析Setter, 并通过Getter检查
            Class<?> getterType = getTypes.get(propName); // 为什么不用Getter获取GetterType?因为Getter可能不存在
            // 对应的Getter方法是否是模棱两可的方法
            boolean isGetterAmbiguous = getMethods.get(propName) instanceof AmbiguousMethodInvoker;
            // 这里就可以理解为什么即使Getter模棱两可了, 也要进行存储
            // 而Setter摸棱两可了, 可以不进行存储
            // 因为对于Setter来说, 对应的Getter如果不存在, 那对Setter不产生影响
            // 而如果Getter如果是摸棱两可, 但是在存储是忽略了, 那么在检查Setter时会认为Getter是不会对Setter产生影响的,
            // 则该Setter会更容易通过, 不符合逻辑
            // 但是, 从代码来看,
            // - 如果不存储摸棱两可的Getter,isGetterAmbiguous就是false
            //   得不到getterType, getterType就是null, setter的参数类型总是不等于getterType
            //   那么直接进入后面判断Setter是否摸棱两可的逻辑
            // - 如果存储了摸棱两可的Getter,isGetterAmbiguous就是true
            //   那么直接进入后面判断Setter是否摸棱两可的逻辑
            // 最终的效果应该是一样的啊! 难道如果不存储Getter, 也依旧能获取到getType吗?
            boolean isSetterAmbiguous = false;
            MethodInvoker match = new AmbiguousMethodInvoker(null, ""); // Getter中的Winner
            for (Method setter : setters) {
                if (!isGetterAmbiguous && setter.getParameterTypes()[0].equals(getterType)) {
                    // 对应的Getter方法不存在或者Getter方法是明确的
                    // 且 Setter的参数类型和Getter的类型一致, 对应上了
                    match = new MethodInvoker(setter);
                    break;  // ? 如果诸多Setter中存在模棱两可的情况, 也要不管不顾了吗 ?
                    // 看来只要和Getter能成功对上, 就不会对Setter的一致性产生怀疑了
                }
                if (!isSetterAmbiguous) {
                    // 可能Getter模棱两可了
                    // 或者Setter的参数类型和Getter的返回类型不一致, 没对应上
                    // 如果当前setter还不是摸棱两可
                    match = pickBetterSetter(match.getMethod(), setter, propName);
                    isSetterAmbiguous = match instanceof AmbiguousMethodInvoker;
                }
                // 如果Setters中出现了摸棱两可的情况
                // 这个Setter就会被忽略
                // 那么match就会是null, 这个配置就不可以通过反射解析Setter写入
                // 🤔: 如果Getter的返回类型和Setter的写入类型不一致....当然可能!
                //          当一个类中维护一个中间字段, 写入一个简单的值,
                //          然后在Setter方法中进行处理, 变成中间类型存储在这个类中
                //          然后在Getter方法中进行处理, 变成一个处理过后的最终类型返回
                //          当然这个配置可能是相同的, 因为它们的意义相同, 但能力却不一样
                //          例如, 输入UserLongin类, 存储为UserId类, 最终返回UserDetailMessage类, 也完全有可能
                //          这几个类也完全可以不带有继承关系
            }

            addSetMethod(propName, match);

        }
    }

    /**
     * 比较现存的setter1和setter2, 看谁更适合做最终的match<br/>
     * 同时, 对两个Setter进行检查, 如果两者存在摸棱两可的关系, 则返回null
     *
     * @return 更适合作为match的setter, 当返回null时表示两个setter的关系是模棱两可的
     */
    private MethodInvoker pickBetterSetter(Method setter1, Method setter2, String propName) {
        if (setter1 == null) {
            return new MethodInvoker(setter2);
        } else if (setter2 == null) {
            return new MethodInvoker(setter1);
        }
        Class<?> paramType1 = setter1.getParameterTypes()[0];
        Class<?> paramType2 = setter2.getParameterTypes()[0];
        if (paramType1.isAssignableFrom(paramType2)) {
            // paramType1是paramType2的父类/父接口/相等
            return new MethodInvoker(setter2); // 返回较小的那个, 儿子总是比父亲有更多的信息
        } else if (paramType2.isAssignableFrom(paramType1)) {
            return new MethodInvoker(setter1);
        }
        // 父亲和儿子总是存在
        return new AmbiguousMethodInvoker(
                setter1, MessageFormat.format(
                "Ambiguous setters defined for property ''{0}'' in class ''{1}'' with types ''{2}'' and ''{3}''.",
                propName,
                setter2.getDeclaringClass().getName(),
                paramType1.getName(),
                paramType2.getName())); // 模棱两可
    }

    private void addSetMethod(String name, MethodInvoker invoker) {
        setMethods.put(name, invoker);
        Type setType = TypeParameterResolver.resolveParamType(invoker.getMethod(), type);
        setTypes.put(name, typeToClass(setType));
    }

    /**
     * 此字段将包括父类字段,
     * 字段是指public修饰的字段, 即使不反射也能有读写的权力,
     * 没有破坏类的封装性
     *
     * @param clazz 如果该类中的字段没有对应的Getter或Setter,
     *              会创建缺失的方法保存一个Invoker到Map里
     */
    private void addFields(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (!setMethods.containsKey(field.getName())) {
                // 有这个字段, 但是没有Setter的, 需要添加注入的方法

                // 摘自源码:
                // issue #379 - removed the check for final because JDK 1.5 allows
                // modification of final fields through reflection (JSR-133). (JGB)
                // pr #16 - final static can only be set by the classloader
                // 问题 379 - 删除了对 final 的检查，
                // 因为 JDK 1.5 允许通过反射 （JSR-133） 修改 final 字段。
                // （JGB） PR 16 - final static只能由类加载器设置
                int modifiers = field.getModifiers();
                if (!(Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers))) {
                    // 不是由 final 且 static 修饰的字段
                    addSetField(field);
                }
            }
            if (!getMethods.containsKey(field.getName())) {
                // 有这个字段, 但是没有Getter的, 需要添加获取的方法
                addGetField(field);
            }
        }
        if (clazz.getSuperclass() != null) {
            // 递归, 存储父类字段
            addFields(clazz.getSuperclass());
        }
    }

    private void addSetField(Field field) {
        if (isValidPropertyName(field.getName())) {
            setMethods.put(field.getName(), new WriteableFieldInvoker(field));
            Type fieldType = TypeParameterResolver.resolveFieldType(field, type);
            setTypes.put(field.getName(), typeToClass(fieldType));
        }
    }

    private void addGetField(Field field) {
        if (isValidPropertyName(field.getName())) {
            getMethods.put(field.getName(), new ReadableFieldInvoker(field));
            Type fieldType = TypeParameterResolver.resolveFieldType(field, type);
            getTypes.put(field.getName(), typeToClass(fieldType));
        }
    }

    /**
     * 解析存储了Getter/Setter方法的Map容器, 获取函数涉及的字段/配置名, 并存入字段`caseInsensitivePropertyMap`
     *
     * @param methodsMap 函数(Getter/Setter)涉及的字段/配置名-函数封装 映射
     * @return 所有函数(Getter / Setter)涉及的字段/配置名
     */
    private String[] analyzePropertyName(Map<String, Invoker> methodsMap) {
        String[] propNames = methodsMap.keySet().toArray(new String[0]);
        // 将配置名存入映射
        for (String propName : propNames) {
            caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
        }
        return propNames;
    }

    public boolean hasDefaultConstructor() {
        return defaultConstructor != null;
    }

    public Constructor<?> getDefaultConstructor() {
        if (defaultConstructor != null) {
            return defaultConstructor;
        } else {
            throw new ReflectionException("There is no default constructor for " + type);
        }
    }


    /**
     * @throws ReflectionException 找不到的情况下抛出异常
     */
    public Invoker getGetInvoker(String propertyName) {
        Invoker method = getMethods.get(propertyName);
        if (method == null) {
            throw new ReflectionException("There is no getter for property named '" + propertyName + "' in '" + type + "'");
        }
        return method;
    }

    /**
     * @throws ReflectionException 找不到的情况下抛出异常
     */
    public Invoker getSetInvoker(String propertyName) {
        Invoker method = setMethods.get(propertyName);
        if (method == null) {
            throw new ReflectionException("There is no setter for property named '" + propertyName + "' in '" + type + "'");
        }
        return method;
    }

    /**
     * @throws ReflectionException 找不到的情况下抛出异常
     */
    public Class<?> getGetterType(String propertyName) {
        Class<?> clazz = getTypes.get(propertyName);
        if (clazz == null) {
            throw new ReflectionException("There is no getter for property named '" + propertyName + "' in '" + type + "'");
        }
        return clazz;
    }

    /**
     * @throws ReflectionException 找不到的情况下抛出异常
     */
    public Class<?> getSetterType(String propertyName) {
        Class<?> clazz = setTypes.get(propertyName);
        if (clazz == null) {
            throw new ReflectionException("There is no setter for property named '" + propertyName + "' in '" + type + "'");
        }
        return clazz;
    }

    /**
     * 通过忽略参数大小写的方式查找其作为字段的配置名
     */
    public String findPropertyName(String name) {
        return caseInsensitivePropertyMap.get(name.toUpperCase(Locale.ENGLISH));
    }

    /**
     * 按名称检查类是否具有可写属性。<br/>
     *
     * @return true: 找到了, 存在该可写属性, {@link Map#containsKey(Object)}
     */
    public boolean hasSetter(String propertyName) {
        return setMethods.containsKey(propertyName);
    }

    /**
     * 按名称检查类是否具有可读属性。<br/>
     *
     * @return true: 找到了, 存在该可读属性, {@link Map#containsKey(Object)}
     */
    public boolean hasGetter(String propertyName) {
        return getMethods.containsKey(propertyName);
    }
}
