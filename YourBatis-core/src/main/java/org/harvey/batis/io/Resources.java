package org.harvey.batis.io;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

/**
 * 依据ClassLoaderWrapper加载资源的工具类,对ClassLoaderWrapper进行读写控制
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-24 10:22
 */
public class Resources {

    private static final ClassLoaderWrapper CLASS_LOADER_WRAPPER = new ClassLoaderWrapper();

    private Resources() {
        // 静态工具类
    }


    public static ClassLoader getDefaultClassLoader() {
        return CLASS_LOADER_WRAPPER.defaultClassLoader;
    }

    public static void setDefaultClassLoader(ClassLoader defaultClassLoader) {
        CLASS_LOADER_WRAPPER.defaultClassLoader = defaultClassLoader;
    }


    /**
     * 用默认ClassLoader加载resource处的URL资源
     *
     * @throws java.io.IOException 资源不可读或未找到
     */
    public static URL getResourceUrl(String resource) throws IOException {
        return getResourceUrl(null, resource);
    }

    /**
     * 用自定义ClassLoader加载resource处的URL资源
     *
     * @throws java.io.IOException 资源不可读或未找到
     */
    public static URL getResourceUrl(ClassLoader loader, String resource) throws IOException {
        URL url = CLASS_LOADER_WRAPPER.getResourceAsUrl(resource, loader);
        if (url == null) {
            throw new IOException("Could not find resource " + resource);
        }
        return url;
    }

    /**
     * 用默认ClassLoader加载resource处的文件流资源
     *
     * @throws java.io.IOException 资源不可读或未找到
     */
    public static InputStream getResourceAsStream(String resource) throws IOException {
        return getResourceAsStream(null, resource);
    }

    /**
     * 用自定义ClassLoader加载resource处的URL资源
     *
     * @throws java.io.IOException 资源不可读或未找到
     */
    public static InputStream getResourceAsStream(ClassLoader loader, String resource) throws IOException {
        InputStream in = CLASS_LOADER_WRAPPER.getResourceAsStream(resource, loader);
        if (in == null) {
            throw new IOException("Could not find resource " + resource);
        }
        return in;
    }


    /**
     * 将resource处资源加载为Properties类
     */
    public static Properties getResourceAsProperties(String resource) throws IOException {
        return Resources.getResourceAsProperties(null, resource);
    }

    /**
     * 用自定义的Classloader将resource处资源加载为Properties类
     */
    public static Properties getResourceAsProperties(ClassLoader loader, String resource) throws IOException {
        Properties props = new Properties();
        try (InputStream in = getResourceAsStream(loader, resource)) {
            props.load(in);
        }
        return props;
    }

    /**
     * 将resource处资源加载为字符输入流
     */
    public static Reader getResourceAsReader(String resource) throws IOException {
        return Resources.getResourceAsReader(null, resource);
    }

    /**
     * 用自定义类加载器将resource处资源加载为字符输入流
     */
    public static Reader getResourceAsReader(ClassLoader loader, String resource) throws IOException {
        return new InputStreamReader(getResourceAsStream(loader, resource));
    }

    public static File getResourceAsFile(String resource) throws IOException {
        return new File(getResourceUrl(resource).getFile());
    }


    public static File getResourceAsFile(ClassLoader loader, String resource) throws IOException {
        return new File(getResourceUrl(loader, resource).getFile());
    }


    public static InputStream getUrlAsStream(String urlString) throws IOException {
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        return conn.getInputStream();
    }


    public static Reader getUrlAsReader(String urlString) throws IOException {
        return new InputStreamReader(getUrlAsStream(urlString));
    }


    public static Properties getUrlAsProperties(String urlString) throws IOException {
        Properties props = new Properties();
        try (InputStream in = getUrlAsStream(urlString)) {
            props.load(in);
        }
        return props;
    }


    public static Class<?> classForName(String className) throws ClassNotFoundException {
        return CLASS_LOADER_WRAPPER.classForName(className);
    }
}