package org.harvey.batis.plugin;

import lombok.AllArgsConstructor;
import org.harvey.batis.annotation.Intercepts;
import org.harvey.batis.annotation.Signature;
import org.harvey.batis.exception.plugin.PluginException;
import org.harvey.batis.util.ReflectionExceptionUnwrappedMaker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-19 22:44
 */
@AllArgsConstructor
public class Plugin implements InvocationHandler {
    private final Object target;
    private final Interceptor interceptor;
    private final Map<Class<?>, Set<Method>> signatureMap;


    public static Object wrap(Object target, Interceptor interceptor) {
        // 解析interceptor的目标类及其目标方法
        Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor);
        Class<?> type = target.getClass();
        // 获取所有被命中的类
        Class<?>[] interfaces = Plugin.getAllHitInterfaces(type, signatureMap);
        if (interfaces.length > 0) {
            return Proxy.newProxyInstance(
                    type.getClassLoader(), // 类加载器
                    interfaces, // 被增强命中的接口
                    new Plugin(target, interceptor, signatureMap) // 增强逻辑
            );
        }
        return target;
    }

    /**
     * @param interceptor 解析类注解是否有{@link Intercepts}之后, 获取其目标方法签名
     * @return 目标方法签名的Map集合
     * @see #phaseMethodMap(Signature[])
     */
    private static Map<Class<?>, Set<Method>> getSignatureMap(Interceptor interceptor) {
        Intercepts interceptsAnnotation = interceptor.getClass().getAnnotation(Intercepts.class);
        if (interceptsAnnotation == null) {
            // 不指定增强谁的Interceptor就没有意义
            throw new PluginException("No @Intercepts annotation was found in interceptor " + interceptor.getClass().getName());
        }
        // 获取目标函数签名
        Signature[] signatures = interceptsAnnotation.value();
        return Plugin.phaseMethodMap(signatures);
    }

    /**
     * @param signatures 注解标识需要被增强的类
     * @return Map(Key : Value = 目标方法所在类类型 : 该目标类中的方法), 有多个目标类
     */
    private static Map<Class<?>, Set<Method>> phaseMethodMap(Signature[] signatures) {
        Map<Class<?>, Set<Method>> signatureMap = new HashMap<>();
        // 遍历解析每一个签名
        for (Signature sig : signatures) {
            // 当前目标类在map中存储的methods
            Class<?> targetType = sig.type();
            Set<Method> methods = signatureMap.computeIfAbsent(targetType, k -> new HashSet<>());
            /*
            相当于:
            Set<Method> methods = signatureMap.get(sig.type());
            if (methods == null) {
                methods = new HashSet();
            }
            signatureMap.put(sig.type(), methods);
            的简便写法
            */
            String methodName = sig.method();
            Class<?>[] methodArgTypes = sig.args();
            try {
                Method method = targetType.getMethod(methodName, methodArgTypes);
                methods.add(method);
            } catch (NoSuchMethodException e) {
                throw new PluginException("Could not find method on " + targetType + " named " + methodName + ". Cause: " + e, e);
            }
        }
        return signatureMap;
    }

    /**
     * @param type         需要被增强的目标类型
     * @param signatureMap 这个interceptor的检测目标方法签名
     * @return type的有关父类/父接口中被当前interceptor命中的父类/父接口
     */
    private static Class<?>[] getAllHitInterfaces(Class<?> type, Map<Class<?>, Set<Method>> signatureMap) {
        // 被命中的interfaces
        Set<Class<?>> hitInterfaces = new HashSet<>();
        while (type != null) {
            for (Class<?> c : type.getInterfaces()) {
                // 遍历当前type的接口
                if (signatureMap.containsKey(c)) {
                    hitInterfaces.add(c);
                }
            }
            // type向上获取父类
            type = type.getSuperclass();
        }
        return hitInterfaces.toArray(new Class<?>[0]);
    }

    /**
     * {@inheritDoc}
     * 被代理时的逻辑
     */
    @Override
    public Object invoke(Object ignored, Method method, Object[] args) throws Throwable {
        try {
            Set<Method> methods = signatureMap.get(method.getDeclaringClass());
            if (methods != null && methods.contains(method)) {
                // 命中, 就是需要被增强的类
                // 执行增强
                return interceptor.intercept(new Invocation(target, method, args));
            }
            // 没命中不增强
            return method.invoke(target, args);
        } catch (Exception e) {
            throw ReflectionExceptionUnwrappedMaker.unwrapThrowable(e);
        }
    }


}
