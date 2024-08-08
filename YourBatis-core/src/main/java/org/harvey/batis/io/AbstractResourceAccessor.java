package org.harvey.batis.io;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-08 13:56
 */
public abstract class AbstractResourceAccessor implements ResourceAccessor {

    /**
     * {@inheritDoc}
     *
     * @see #getResources(String)
     * @see #list(URL, String)
     */
    @Override
    public List<String> list(String path) throws IOException {
        List<String> names = new ArrayList<>(); // 结果集
        for (URL url : AbstractResourceAccessor.getResources(path)) {
            names.addAll(this.list(url, path));
        }
        return names;
    }

    /**
     * 从上下文ClassLoader获取所有指定路径上的资源, 然后所有资源将被作为{@link URL}集合返回
     *
     * @param path 资源路径, 例如:
     *             <pre>{@code "org/harvey/batis/util"}</pre>
     * @return {@link URL} List, 即 {@link ClassLoader#getResources(String)} 的返回值, 例如:
     * <pre>{@code "file:/D:/IT_study/source/JDK/YourBatis/YourBatis-core/target/test-classes/org/harvey/batis/util"}</pre>
     * @see ClassLoader#getResources(String)
     */
    protected static List<URL> getResources(String path) throws IOException {
        return Collections.list(Thread.currentThread().getContextClassLoader().getResources(path));
    }

    /**
     * 递归找出本级所有资源及其子资源中的一切资源
     *
     * @param url     由 URL 标识的资源的路径, 形如
     *                <pre>{@code "file:/D:/IT_study/source/JDK/YourBatis/YourBatis-core/target/test-classes/org/harvey/batis/util"}</pre>
     * @param forPath 传递给 {@link #getResources(String)} 以获取资源 URL 的Path, 形如:
     *                <pre>{@code "org/harvey/batis/util"}</pre>
     * @return 本级资源, 及其子资源的路径, 型如:
     * <pre>{@code "org/harvey/batis/util/StrictMapTest.class"}</pre>
     */
    protected abstract List<String> list(URL url, String forPath) throws IOException;

}
