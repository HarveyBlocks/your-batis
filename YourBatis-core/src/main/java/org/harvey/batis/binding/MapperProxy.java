package org.harvey.batis.binding;

import org.harvey.batis.exception.UnfinishedFunctionException;
import org.harvey.batis.session.SqlSession;
import org.harvey.batis.util.ReflectionExceptionUnwrappedMaker;

import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * 给出了实现MapperInterface的方法的方式, 即依据Mapper.xml实现
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-02 13:36
 * @see #wrap2MapperMethodInvoker
 * @see PlainMethodInvoker
 * @see DefaultMethodInvoker
 */
public class MapperProxy<T> implements InvocationHandler, Serializable {
    // 继承Serializable干嘛?

    /**
     * MethodHandles生产出的Lookup能访问的作用域
     */
    private static final int ALLOWED_MODES = MethodHandles.Lookup.PRIVATE |
            MethodHandles.Lookup.PROTECTED |
            MethodHandles.Lookup.PACKAGE |
            MethodHandles.Lookup.PUBLIC;

    /**
     * 该方法能无视权限, 创造出任何作用域的MethodHolder
     *
     * @see MethodHandles#privateLookupIn(Class, MethodHandles.Lookup)
     */
    private static final Method PRIVATE_LOOKUP_IN;
    private static final Constructor<MethodHandles.Lookup> LOOKUP_CONSTRUCTOR;
    private static final boolean VERSION_OVER_JAVA8;

    static {
        PRIVATE_LOOKUP_IN = MapperProxy.getPrivateLookupIn();
        VERSION_OVER_JAVA8 = PRIVATE_LOOKUP_IN != null;
        LOOKUP_CONSTRUCTOR = VERSION_OVER_JAVA8 ? null : MapperProxy.getLookupConstructor();
    }


    /**
     * Java9以上就能获取{@link MethodHandles#privateLookupIn(Class, MethodHandles.Lookup)}的{@link Method}
     *
     * @return {@link MethodHandles#privateLookupIn(Class, MethodHandles.Lookup)}的{@link Method},<br>
     * 不能获取就返回null
     */
    private static Method getPrivateLookupIn() {
        try {
            return MethodHandles.class
                    .getMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * Java8以下不能获取{@link MethodHandles#privateLookupIn(Class, MethodHandles.Lookup)}的{@link Method},
     * 只好获取{@link MethodHandles.Lookup}的非Public构造器
     *
     * @return {@link MethodHandles#privateLookupIn(Class, MethodHandles.Lookup)}的{@link Method},<br>
     * 不能获取就返回null, 这个一般是没有权限获取非Public的构造器之类的
     * @throws IllegalStateException 如果构造器也没有, 就抛出异常
     */
    private static Constructor<MethodHandles.Lookup> getLookupConstructor() {
        try {
            Constructor<MethodHandles.Lookup> lookup = MethodHandles.Lookup.class
                    .getDeclaredConstructor(Class.class, int.class);
            lookup.setAccessible(true);
            return lookup;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                    "There is neither 'privateLookupIn(Class, Lookup)' " +
                            "nor 'Lookup(Class, int)' method " +
                            "in java.lang.invoke.MethodHandles.", e);
        } catch (Exception e) {
            return null;
        }
    }

    private final SqlSession sqlSession;
    private final Class<T> mapperInterface;
    /**
     * 享元, 存储已经被包装了的{@link MapperMethodInvoker}
     */
    private final Map<Method, MapperMethodInvoker> methodCache;

    public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethodInvoker> methodCache) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
        this.methodCache = methodCache;
    }

    /**
     * @see #cachedInvoker(Method)
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            if (Object.class.equals(method.getDeclaringClass())) {
                // 如果是Object原生的函数, 直接执行
                return method.invoke(this, args);
            } else {
                // 增强后执行
                return this.cachedInvoker(method).invoke(proxy, method, args, sqlSession);
            }
        } catch (Throwable t) {
            throw ReflectionExceptionUnwrappedMaker.unwrapThrowable(t);
        }
    }

    /**
     * 如果{@link #methodCache}中存在{@link MapperMethodInvoker}, 直接返回<br>
     * 否则将参数的 {@param method} 反射, 包装成{@link MapperMethodInvoker}<br>
     * 但是包装之后, 将MapperMethodInvoker存入{@link #methodCache}
     *
     * @param method 获取其MapperMethodInvoker
     */
    private MapperMethodInvoker cachedInvoker(Method method) throws Throwable {
        try {
            // 获取执行对象
            MapperMethodInvoker invoker = methodCache.get(method);
            if (invoker != null) {
                return invoker;
            }
            // 如果不存在，则执行映射, 返回的结果, 是映射后的结果
            // 而且, 如果不存在, 且(value=wrap2MapperMethodInvoker(key))!=null, 将这一组(key, value)存入methodCache
            // 类似于懒加载
            return methodCache.computeIfAbsent(method, this::wrap2MapperMethodInvoker);
        } catch (RuntimeException re) {
            // 解除里面抓住的RuntimeException包装
            Throwable cause = re.getCause();
            throw cause == null ? re : cause;
        }
    }

    /**
     * 依据Java8和Java9不同版本, 用不同的反射方法, 将Method包装成{@link MapperMethodInvoker}后返回
     *
     * @throws RuntimeException 可能是包装了{@link IllegalAccessException},
     *                          或{@link InstantiationException},
     *                          或{@link InvocationTargetException},
     *                          或{@link java.util.NoSuchElementException}
     */
    private MapperMethodInvoker wrap2MapperMethodInvoker(Method method) {
        // 默认方法是指公共非抽象实例方法
        // 即具有方法体的非静态方
        if (!method.isDefault()) {
            // 是抽象方法, 需要用代理实现这个方法, 认为是Mapper的方法
            // TODO
            MapperMethod mapperMethod = new MapperMethod(mapperInterface, method, sqlSession.getConfiguration());
            // 包装这个代理类, 返回
            return new PlainMethodInvoker(mapperMethod);
        }
        // 是有具体函数体的方法, 包装后返回
        // 使用DefaultMethodInvoker
        try {
            return new DefaultMethodInvoker(
                    VERSION_OVER_JAVA8 ? getMethodHandleJava9(method) : getMethodHandleJava8(method));
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException
                 | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 用{@link MethodHandles#privateLookupIn(Class, MethodHandles.Lookup)}<br>
     * 将{@link Method}转为{@link MethodHandle}
     */
    private MethodHandle getMethodHandleJava9(Method method)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        Objects.requireNonNull(PRIVATE_LOOKUP_IN);
        final Class<?> declaringClass = method.getDeclaringClass();
        // null.privateLookupIn(declaringClass,lookup), 因为是静态方法, 所以没人调用
        MethodHandles.Lookup lookup = (MethodHandles.Lookup) PRIVATE_LOOKUP_IN
                .invoke(null, declaringClass, MethodHandles.lookup());
        MethodType methodType = MapperProxy.method2MethodType(method);
        // 要求调用findSpecial的lookup和declaringClass一致
        // 而本lookup通过declaringClass创建
        // 🤔 : 还是没搞懂findSpecial的作用
        return lookup.findSpecial(
                declaringClass,
                method.getName(),
                methodType,
                declaringClass);
    }

    /**
     * 用{@link Method}获取指定的{@link MethodType}
     */
    private static MethodType method2MethodType(Method method) {
        return MethodType.methodType(method.getReturnType(), method.getParameterTypes());
    }

    /**
     * 用{@link #LOOKUP_CONSTRUCTOR}将{@link Method}反射成{@link MethodHandle}
     */
    private MethodHandle getMethodHandleJava8(Method method)
            throws IllegalAccessException, InstantiationException, InvocationTargetException {
        Objects.requireNonNull(LOOKUP_CONSTRUCTOR);
        final Class<?> declaringClass = method.getDeclaringClass();
        return LOOKUP_CONSTRUCTOR.newInstance(declaringClass, ALLOWED_MODES)
                .unreflectSpecial(method, declaringClass);
    }

    /**
     * 用代理, 对方法进行调用执行
     */
    public interface MapperMethodInvoker {
        /**
         * 实现该方法, 以实现不同的代理方式
         *
         * @param proxy      执行方法的实例对象
         * @param method     被执行的方法
         * @param args       被执行方法的参数
         * @param sqlSession 必要的数据库连接
         */
        Object invoke(Object proxy, Method method, Object[] args, SqlSession sqlSession) throws Throwable;
    }

    /**
     * TODO
     * 代理类实现Mapper接口的抽象方法
     *
     * @see #invoke(Object, Method, Object[])
     */
    private static class PlainMethodInvoker implements MapperMethodInvoker {
        /**
         * 被包装的抽象接口
         */
        private final MapperMethod mapperMethod;

        public PlainMethodInvoker(MapperMethod mapperMethod) {
            super();
            this.mapperMethod = mapperMethod;
        }

        /**
         * TODO
         *
         * @see MapperMethod
         * @see MapperMethod#execute(SqlSession, Object[])
         */
        @Override
        public Object invoke(Object ignoreObj, Method ignoreMethod, Object[] args, SqlSession sqlSession) throws Throwable {
            return mapperMethod.execute(sqlSession, args);
        }
    }

    /**
     * 通过{@link MethodHandle}对方法调用
     */
    private static class DefaultMethodInvoker implements MapperMethodInvoker {
        private final MethodHandle methodHandle;

        public DefaultMethodInvoker(MethodHandle methodHandle) {
            super();
            this.methodHandle = methodHandle;
        }

        /**
         * proxy.{@link #methodHandle}(args)
         */
        @Override
        public Object invoke(Object proxy, Method ignoreMethod, Object[] args, SqlSession ignoreSqlSession)
                throws Throwable {
            return methodHandle.bindTo(proxy).invokeWithArguments(args);
        }
    }

}
