package org.harvey.batis.io.xml;

import lombok.Getter;
import lombok.Setter;
import org.harvey.batis.io.ResourceAccessorFactory;
import org.harvey.batis.io.log.Log;
import org.harvey.batis.io.log.LogFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>用于查找类路径中可用并满足任意条件的类。
 * 最常见的两个条件是一个类实现扩展另一个类，
 * TODO 或者它使用特定的注释进行注释。
 * 但是，通过实现 {@link ClassMatcher}类，可以使用任意条件进行检查, 搜索。</p>
 *
 * <p>ClassLoader 用于在类路径中查找 TODO 包含某些包中的类的所有位置(目录和jar文件)，
 * 然后加载这些类并检查它们。
 * 默认情况下，使用
 * <pre>{@code Thread.currentThread().getContextClassLoader()}</pre>
 * 返回的 ClassLoader，
 * 但可以通过在调用任何{@link #find(ClassMatcher, String)}方法之前,
 * 先调用 {@link #setClassLoader(ClassLoader)} 来覆盖它。</p>
 *
 * <p>
 * 通过调用 {@link #find(ClassMatcher, String)} 方法并提供包名称和 {@link ClassMatcher} 实例来启动搜索。<br>
 * 这将导致扫描命名包<b>和所有子包</b>以查找满足测试的类。<br>
 * 对于扫描多个包以查找特定类的扩展
 * TODO 或使用特定注释注释的类的常见用例，还有一些实用方法。<br>
 * </p>
 *
 * <p>以下是标准的使用模式</p>
 *
 * <pre>{@code
 *   ResolverUtil<ActionBean> resolver = new ResolverUtil<ActionBean>();
 *   resolver.find(new ClassMatcherImpl(), pkg1);
 *   resolver.find(new ClassMatcherImpl(), pkg2);
 *   Collection<ActionBean> beans = resolver.getMatches();
 * }</pre>
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-07 16:54
 */
@Getter
public class ResolverUtil<T> {
    private static final Log LOG = LogFactory.getLog(ResolverUtil.class);
    /**
     * 如果通过了{@link ClassMatcher}的检查的类, 回被存储到本字段
     */
    private final Set<Class<? extends T>> matches = new HashSet<>();

    /**
     * 在查找类, 然后实例化的过程中回使用到的{@link ClassLoader}<br>
     * 当classloader为null时将会使用 {@link Thread#currentThread()}的{@link Thread#getContextClassLoader()}<br>
     * 你把它设置成了null, 它就会使用{@link Thread#getContextClassLoader()}<br>
     */
    @Setter
    private ClassLoader classLoader;

    /**
     * @see #classLoader
     */
    public ClassLoader getClassLoader() {
        /* 🤔 : 为什么不采用这种, 少调用了几次{@link Thread#getContextClassLoader()}函数
           答 : 因为当classloader被指定为contextClassLoader后, 就不是null了
                然后如果这个实例对象去了另外一个线程,
                本classloader不是null, 就不能切换成这个线程的contextClassLoader, 还是老线程的contextClassLoader
                就是在一个线程使用另外一个线程的contextClassLoader了
        if (classloader == null) {
            classloader = Thread.currentThread().getContextClassLoader();
        }
        return classloader;*/
        return classLoader == null ? Thread.currentThread().getContextClassLoader() : classLoader;
    }

    /**
     * @param matcher     {@link ClassMatcher}的实现类, 用来筛选被选中的类
     * @param packageName 包名<br>
     *                    <pre>{@code "org.harvey.batis.demo.mapper"}</pre>
     *                    被转换为路径后扫描路径下的资源(包括一般文件和字节码文件)<br>
     *                    本方法中, 被扫描到的资源, 如果是字节码文件, <br>
     *                    且被{@link ResolverUtil.ClassMatcher#matches}捕获<br>
     *                    将被保存在{@link #matches}<br>
     *                    通过{@link #getMatches()}来获取{@link #matches}
     * @return 本对象
     * @see #addIfMatching(ClassMatcher, String)
     */
    public ResolverUtil<T> find(ClassMatcher matcher, String packageName) {
        String path = this.package2Path(packageName);
        try {
            // 访问应用程序服务器中的资源。
            List<String> children = ResourceAccessorFactory
                    .getInstance().list(path);
            for (String child : children) {
                if (child.endsWith(".class")) {
                    this.addIfMatching(matcher, child);
                }
            }
        } catch (IOException ioe) {
            LOG.error("Could not read package: " + packageName, ioe);
        }
        return this;
    }

    /**
     * 将 Java 包名称转换为文件路径(所有{@code .}换成{@code /}), <br>
     * 方便调用{@link ClassLoader#getResources(String)}, 获取资源
     *
     * @param packageName Java包名称
     * @return the package 文件路径
     */
    protected String package2Path(String packageName) {
        return packageName == null ? null : packageName.replace('.', '/');
    }

    /**
     * 依据全限定名获取Class字节码类, 然后看这个类能否被matcher获取, 能就存入{@link #matches}
     *
     * @param matcher   符合标准的类将会被获取, 存入{@link #matches}
     * @param classpath 类的全限定名, 形如:
     *                  <pre>{@code "org/harvey/batis/demo/mapper/EmployeeMapper.class"}</pre>
     */
    protected void addIfMatching(ClassMatcher matcher, String classpath) {
        String classFullname = classpath.substring(0, classpath.lastIndexOf('.'))
                // 此`.`乃`.class`之`.`
                // 因为前面的部分已经由包的`.`转换为路径的`/`了
                // 现在再转换回来
                .replace('/', '.');
        // 相当于获取了全类名
        try {
            ClassLoader loader = this.getClassLoader();
            LOG.debugIfEnable("Checking to see if class " + classFullname +
                    " matches criteria [" + matcher + "]");
            // classFullname实例化出来了type
            Class<?> type = loader.loadClass(classFullname);
            if (matcher.matches(type)) {
                // type符合标准
                // 转换为(Class<T>)之后存入结果集合
                // 问题是, 符合标准的classFullname, 都是T及其子类吗?
                matches.add((Class<T>) type);
            }
        } catch (Throwable t) {
            LOG.warn("Could not examine class '" + classpath + "'" + " due to a "
                    + t.getClass().getName() + " with message: " + t.getMessage());
        }
    }

    /**
     * 用于检查某些类是否符合某些标准
     */
    @FunctionalInterface
    public interface ClassMatcher {

        /**
         * @param type 被检查的类
         * @return 如果类通过了检验, 符合了标准, 则返回true
         */
        boolean matches(Class<?> type);
    }

    /**
     * {@inheritDoc}
     *
     * @see ResolverUtil.IsSonMatcher#matches
     */
    public static class IsSonMatcher implements ClassMatcher {
        /**
         * 父类
         */
        private final Class<?> parent;

        /**
         * @param parentType 将提供的类作为父类/父类接口 {@link #parent}
         */
        public IsSonMatcher(Class<?> parentType) {
            this.parent = parentType;
        }

        /**
         * {@inheritDoc}
         *
         * @param type 检查本参数是否是{@link #parent}的子类/子实现类
         * @return 如果是子类/子实现类, 返回true; <br>
         * 如果是本类, 返回true; <br>
         * 否则, 返回false
         */
        @Override
        public boolean matches(Class<?> type) {
            return type != null && parent.isAssignableFrom(type);
        }

        /**
         * @return 用于查看本类是用于检查哪个类的子类的
         */
        @Override
        public String toString() {
            return "is assignable to " + parent.getSimpleName();
        }
    }


}
