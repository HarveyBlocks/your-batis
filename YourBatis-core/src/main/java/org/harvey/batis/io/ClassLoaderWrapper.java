package org.harvey.batis.io;

import java.io.InputStream;
import java.net.URL;
import java.util.function.BiFunction;

/**
 * 通过类加载器获取resource路径上的资源
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-24 10:24
 */
public class ClassLoaderWrapper {
    ClassLoader defaultClassLoader;
    ClassLoader systemClassLoader;

    ClassLoaderWrapper() {
        try {
            systemClassLoader = ClassLoader.getSystemClassLoader();
        } catch (SecurityException ignored) {
            // 由于权限限制, 没有获取到systemClassLoader
        }
    }

    public InputStream getResourceAsStream(String resource) {
        return getResourceAsStream(resource, null);
    }

    public InputStream getResourceAsStream(String resource, ClassLoader loader) {
        return getResourceAsStream0(resource, getClassLoaders(loader));
    }

    private static InputStream getResourceAsStream0(String resource, ClassLoader[] classLoader) {
        return getResourceAs(resource, classLoader, ClassLoader::getResourceAsStream);
    }


    /**
     * @see #getResourceAs(String, ClassLoader[], BiFunction)
     */
    public URL getResourceAsUrl(String resource) {
        return getResourceAsUrl0(resource, null);
    }

    /**
     * @see #getClassLoaders(ClassLoader)
     * @see #getResourceAs(String, ClassLoader[], BiFunction)
     */
    public URL getResourceAsUrl(String resource, ClassLoader loader) {
        return getResourceAsUrl0(resource, getClassLoaders(loader));
    }


    /**
     * {@link ClassLoader#getResources(String)}的参数<br>
     * <pre>{@code String[] paths = {
     *      "org/harvey/batis/util/StrictMapTest.class",// 可
     *      "org/harvey/batis/util/StrictMapTest", // 不可
     *      "/org/harvey/batis/util/StrictMapTest.class", // 不可
     *      "/org/harvey/batis/util/StrictMapTest", // 不可
     *      "/org/harvey/batis/util/", // 不可
     *      "/org/harvey/batis/util", // 不可
     *      "org/harvey/batis/util/StrictMapTest.java", // 不可
     *      "org/harvey/batis/util/StrictMapTest.yml", // 可
     *      "org/harvey/batis/util/", // 可, 参数末尾有`/`, 结果就有`/`
     *      "org/harvey/batis/util", // 可, 参数末尾无`/`, 结果就无`/`
     * };}</pre>
     * 其返回值是一个可迭代的URL集合, 形如:
     * <pre>{@code file:/D:/IT_study/source/JDK/YourBatis/YourBatis-core/target/test-classes/org/harvey/batis/util/StrictMapTest.yml}</pre>
     * @see #getResourceAs(String, ClassLoader[], BiFunction)
     * @see ClassLoader#getResources(String)
     */
    private static URL getResourceAsUrl0(String resource, ClassLoader[] classLoader) {
        return ClassLoaderWrapper.getResourceAs(resource, classLoader, ClassLoader::getResource);
    }

    /**
     * 依据resourceSupplier获取不同类型的resource
     *
     * @return the resource or null
     */
    private static <R> R getResourceAs(String resource, ClassLoader[] classLoader, BiFunction<ClassLoader, String, R> resourceSupplier) {
        R returnValue = null;
        if (classLoader == null) {
            return returnValue;
        }
        for (ClassLoader cl : classLoader) {
            if (cl == null) {
                continue;
            }
            returnValue = resourceSupplier.apply(cl, resource);
            if (null == returnValue) {
                returnValue = resourceSupplier.apply(cl, "/" + resource);
            }
            if (returnValue != null) {
                break;
            }
        }
        return returnValue;
    }

    /**
     * @see #classForName(String, ClassLoader)
     */
    public Class<?> classForName(String className) throws ClassNotFoundException {
        return this.classForName(className, null);
    }

    /**
     * @see #classForName0
     */
    public Class<?> classForName(String name, ClassLoader classLoader) throws ClassNotFoundException {
        return classForName0(name, this.getClassLoaders(classLoader));
    }

    /**
     * 尝试用classLoader加载一个类
     */
    private static Class<?> classForName0(String name, ClassLoader[] classLoader) throws ClassNotFoundException {
        for (ClassLoader cl : classLoader) {
            if (cl == null) {
                continue;
            }
            try {
                return Class.forName(name, true, cl);
            } catch (ClassNotFoundException ignore) {
                // 忽略本次无法找到的类, 直到所有类加载器都无法找到该类
            }
        }
        throw new ClassNotFoundException("Cannot find class: " + name);
    }

    /**
     * 将几个Classloader放入一个数组
     */
    ClassLoader[] getClassLoaders(ClassLoader classLoader) {
        return new ClassLoader[]{classLoader, // 参数指定的加载器
                defaultClassLoader, // 默认加载器, 由外界指定
                Thread.currentThread().getContextClassLoader(), // 线程的加载器
                this.getClass().getClassLoader(), // 本类的类加载器
                systemClassLoader // 系统加载器
        }; // 存在优先级
    }


}
