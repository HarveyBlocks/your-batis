package org.harvey.batis.io;

import org.harvey.batis.io.log.Log;
import org.harvey.batis.io.log.LogFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * {@inheritDoc}
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-07 17:36
 */
public class DefaultResourceAccessor extends AbstractResourceAccessor {

    private static final Log LOG = LogFactory.getLog(DefaultResourceAccessor.class);
    /**
     * Jar包的魔数
     */
    private static final byte[] JAR_MAGIC = {'P', 'K', 3, 4};

    @Override
    public boolean isValid() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @param url  文件夹的URL, 或Jar包的URL, 或Jar包内文件夹的URL
     * @param path 文件夹的路径, 或Jar包的路径, 或Jar包内文件夹的路径
     * @return
     */
    @Override
    protected List<String> list(URL url, String path) throws IOException {
        // 尝试查找包含所请求资源的 JAR 文件的 URL。
        // 如果找到 JAR 文件，我们将通过读取 JAR 来列出子资源。
        URL jarUrl = this.findJarForResource(url);
        if (jarUrl != null) {// 是Jar的URL, 那么此时
            // jarUrl是Jar包的地址
            // path是jar内的文件的路径
            // 例如
            // url = "jar:file:/D:/IT_study/maven/repository/ch/qos/logback/logback-core/1.2.3/logback-core-1.2.3.jar!/ch/qos/logback/core"
            // jarUrl = "file:/D:/IT_study/maven/repository/ch/qos/logback/logback-core/1.2.3/logback-core-1.2.3.jar"
            // path = "ch/qos/logback/core"
            // 如果单独调用此方法(url和path毫无关系的情况)也可能是:
            // url = "file:/D:/IT_study/maven/repository/ch/qos/logback/logback-core/1.2.3/logback-core-1.2.3.jar"
            // jarUrl = "file:/D:/IT_study/maven/repository/ch/qos/logback/logback-core/1.2.3/logback-core-1.2.3.jar"
            // path = ""
            // 那就是jar包里所有的文件了
            // 当然, 如果是通过list(path)间接调用此方法, path="",
            // 如果ClassLoader#getResource得到的URL在Jar里, 就是全部文件,
            // 如果不在jar里, 由于后面会"/"+path, 导致无法再次从getResource()获取资源
            // 因为getResource要求资源路径不以"/"开头
            try (InputStream is = jarUrl.openStream();
                 // is是Jar包的流
                 JarInputStream jarIs = new JarInputStream(is)) {
                LOG.debugIfEnable("Listing " + url);
                return this.listResources(jarIs, path);
            }
        }
        List<String> children = this.childDictionaryAfterPath(url, path);
        String prefix = url.toExternalForm();
        if (!prefix.endsWith("/")) {
            // 为URL的字符串加上`/`
            prefix = prefix + "/";
        }
        if (!path.endsWith("/")) {
            // 为path的字符串加上`/`
            path = path + "/";
        }
        // 遍历直接子级，添加文件到目录中
        List<String> resources = new ArrayList<>();
        for (String child : children) {
            String resourcePath = path + child;
            resources.add(resourcePath); // 加上本级
            // 这个本级是文件, 也有可能是文件夹
            // 由于path从外界传入时, 一定是文件夹
            // 如果是文件直接返回emptyList
            // 如果是文件夹, 文件夹下又有文件
            // 该文件就会在children集合里
            // child存一遍,
            // 如果文件的路径作为参数再传入list进行递归的时候
            // 返回emptyList, 就不会重复加入resources集合了
            URL childUrl = new URL(prefix + child);
            // 递归
            resources.addAll(this.list(childUrl, resourcePath));
        }
        return resources;
    }

    /**
     * 自己实现的获取所有文件(不包含目录的方法)
     *
     * @param path 必须没有前导/和后导/<br>
     *             是对于Java的package来说的路径, 而不是什么相对路径或绝对路径<br>
     * @return 集合, path="com/harvey/util", 返回"com/harvey/util/StringUtil.class"等
     */
    protected List<String> list(String path, URL url) throws IOException {
        URL jarUrl = this.findJarForResource(url);
        if (jarUrl != null) {
            try (InputStream is = jarUrl.openStream();
                 JarInputStream jarIs = new JarInputStream(is)) {
                LOG.debugIfEnable("Listing " + url);
                return this.listResources(jarIs, path);
            }
        }
        if (!"file".equals(url.getProtocol())) {
            throw new FileNotFoundException(url.toExternalForm());
        }
        File file = new File(url.getFile());
        LOG.debugIfEnable("Listing directory " + file.getAbsolutePath());
        if (!file.isDirectory()) {
            return List.of(path);
        }
        LOG.debugIfEnable("Listing " + url);
        String[] list = file.list();
        List<String> children = new ArrayList<>();
        if (list != null) {
            children.addAll(Arrays.asList(list));
        }
        String prefix = url.toExternalForm();
        if (!prefix.endsWith("/")) {
            prefix = prefix + "/";
        }
        List<String> resources = new ArrayList<>();
        for (String child : children) {
            String resourcePath = path + "/" + child;
            URL childUrl = new URL(prefix + child);
            resources.addAll(this.list(resourcePath, childUrl));
        }
        return resources;
    }


    /**
     * 解构给定的 URL 查找包含 URL 引用的资源的 JAR 文件.<br>
     * 假设 URL 中有引用 JAR, 返回 JAR 文件本身的 URL.<br>
     * 如果无法找到 JAR，则返回 null<br>
     *
     * @param dicUrl 可以是Jar包内的文件夹, 耶可以是Jar包本身.<br>
     *               如果指向Jar包内的文件, 是这样的:
     *               <pre>{@code "jar:file:/D:/IT_study/maven/repository/ch/qos/logback/logback-core/1.2.3/logback-core-1.2.3.jar!/ch/qos/logback/core"}</pre>
     * @return 如果不是Jar包, 或不在Jar包内, 返回null, 是就返回Jar包的地址URL
     */
    protected URL findJarForResource(URL dicUrl) {
        LOG.debugIfEnable("Find JAR URL: " + dicUrl);
        // 如果 URL 的文件部分本身就是文件的URL, 而不是文件夹的URL
        // 则该 URL 可能就是 JAR
        while (true) {
            try {
                // 🤔 : 为什么要这么做? 为的是应对哪种情况?
                // 答 : 当file真的是file而不能用来组成url的时候,
                //      说明当前url成了
                //      类似于解包的样子
                //      0 : jar:file:/D:/IT_s....
                //      1 : file:/D:/IT_s....
                //      2 : /D:/IT_s.... 不能组成URL, 则file:/D:/IT_s....即想要的URL
                String file = dicUrl.getFile();
                dicUrl = new URL(file);
                LOG.debugIfEnable("Inner URL: " + dicUrl);
            } catch (MalformedURLException e) {
                // MalformedURLException 表示发生了格式错误的 URL
                // 在字符串中找不到有效的协议，或者无法解析该字符串。
                // 不正常说明url的file部分就是文件路径了
                break;
            }
        }
        // URL#toExternalForm == URL#toString
        StringBuilder jarUrlStringBuilder = new StringBuilder(dicUrl.toExternalForm());
        // 获取.jar后缀的位置
        int index = jarUrlStringBuilder.lastIndexOf(".jar");
        if (index < 0) {
            // 没有.jar的结尾, 直接认定不是jar包
            LOG.debugIfEnable("Not a JAR: " + jarUrlStringBuilder);
            return null;
        }
        // 有.jar的文件后缀
        // 提取Jar的URL(去除jar包路径后面的内容)
        // .../qos/logback/logback-core-1.2.3.jar!/ch/qos/logback/core ->
        // .../qos/logback/logback-core-1.2.3.jar
        jarUrlStringBuilder.setLength(index + 4);
        LOG.debugIfEnable("Extracted JAR URL: " + jarUrlStringBuilder);


        try {
            // 可能是Jar包的URL
            // 删去后面的内容后, 重新获取Jar包本身的URL
            URL mayJarUrl = new URL(jarUrlStringBuilder.toString());
            if (isJar(mayJarUrl)) {
                // 是Jar包
                return mayJarUrl;
            }
            // 由于检查魔数需要打开文件, 可能文件打不开, 但是文件内的魔数是正确的
            // 打不开的原因可能是文件不存在?
            // 认为文件不存在可能是URL的编码原因造成的:
            // 检查URL的文件是否存在于本系统文件中,
            // URL中获取文件路径->用文件路径打开文件->文件存在则获取该文件的URL->返回URL
            LOG.debugIfEnable("Not a JAR: " + jarUrlStringBuilder);
            // jarUrlStringBuilder 被赋值文件路径部分
            // 例如去掉前面的"file:"等协议部分
            jarUrlStringBuilder.replace(0, jarUrlStringBuilder.length(), mayJarUrl.getFile());
            File file = new File(jarUrlStringBuilder.toString());
            // file name might be url-encoded
            if (!file.exists()) {
                // 文件名不存在?可能是由于jarUrlStringBuilder字符串的编码格式不是URL
                // URLEncoder#encode, URL原来是StandardCharsets.UTF_8编码格式, 转换为URL的编码格式
                // "Hello,World!"->"Hello%2CWorld%21"
                // 然后用URL的编码格式去打开文件
                file = new File(URLEncoder.encode(jarUrlStringBuilder.toString(), StandardCharsets.UTF_8));
            }
            if (file.exists()) {
                LOG.debugIfEnable("Trying real file: " + file.getAbsolutePath());
                // 🤔 : WHY? 因为file#URL()被删除的缘故吗?
                URI uri = file.toURI();
                mayJarUrl = uri.toURL();
                if (isJar(mayJarUrl)) {
                    return mayJarUrl;
                }
            }
            // 此时如果还打不开, 就是文件真的不存在了
        } catch (MalformedURLException e) {
            // 无效的URL：
            LOG.warn("Invalid JAR URL: " + jarUrlStringBuilder);
        }

        LOG.debugIfEnable("Not a JAR: " + jarUrlStringBuilder);
        return null;
    }

    /**
     * 从Jar包中获取path资源
     * path的资源一定在Jar包中
     *
     * @param jarIs   指定jar包的InputStream
     * @param dicPath jar包中的资源(Jar包内的文件夹)的路径(相对路径)
     * @return path指向的在jar包中的资源(指path本身下的所有资源及其所有子包下的所有资源)<br>
     * 包括class字节码文件和其他文件, 不包括文件夹, 形如:<br>
     * <pre>{@code "org/harvey/batis/demo/mapper/EmployeeMapper.xml"}</pre>
     * 没有前导"/", 有文件后缀
     */
    protected List<String> listResources(JarInputStream jarIs, String dicPath) throws IOException {
        // 如果path没有前面的"/"和后面的"/", 就补上
        if (!dicPath.startsWith("/")) {
            dicPath = "/" + dicPath;
        }
        if (!dicPath.endsWith("/")) {
            dicPath = dicPath + "/";
        }

        List<String> resources = new ArrayList<>();
        JarEntry entry;
        // 扫描Jar包内所有的资源
        while ((entry = jarIs.getNextJarEntry()) != null) {
            if (entry.isDirectory()) {
                // 是目录, 继续
                continue;
            }
            // 是文件
            // name型如"/org/harvey/batis/demo/mapper/EmployeeMapper.xml"
            StringBuilder name = new StringBuilder(entry.getName());
            // 添加前面的"/"
            if (name.charAt(0) != '/') {
                name.insert(0, '/');
            }
            if (name.indexOf(dicPath) == 0) {
                // 是指定的path开头的
                LOG.debugIfEnable("Found resource: " + name);
                // Trim leading slash
                resources.add(name.substring(1));
            }
        }
        return resources;
    }


    /**
     * 获取所有子目录, 对于当前目录下的文件, 视而不见
     *
     * @param path 文件夹的url或jar包的目录
     * @param url  文件夹的url或jar包
     * @return 如果path和url是具体的文件, 就返回{@link Collections#emptyList()}<br>
     * 如果是Jar包, 则返回一切元素<br>
     * 如果是文件夹返回本级元素
     * 要求不含有前导`/`
     */
    private List<String> childDictionaryAfterPath(URL url, String path) throws IOException {
        // 当前文件夹下的一切元素(包括文件和文件夹)的Path
        try {
            if (isJar(url)) {
                // 🤔 : 我认为永远不会进入该分支
                List<String> children = new ArrayList<>();
                try (InputStream is = url.openStream();
                     JarInputStream jarInput = new JarInputStream(is)) {
                    LOG.debugIfEnable("Listing " + url);
                    JarEntry entry;
                    // 所有的实体, 包括文件目录和文件
                    while ((entry = jarInput.getNextJarEntry()) != null) {
                        LOG.debugIfEnable("Jar entry: " + entry.getName());
                        children.add(entry.getName());
                    }
                    return children;
                }
            } else {
                // 一些 servlet 容器允许从目录资源中读取
                // (Tomcat不允许, 抛出FileNotFoundException)
                // 并每行列出一个子资源。
                // 但是，无法仅通过读取目录和文件资源来区分它们。
                // 那么, 在读取每一行时，通过类加载器将其作为当前资源进行查找。
                // 如果任何一行失败, 则假设当前资源不是目录
                List<String> children = new ArrayList<>();
                try (InputStream is = url.openStream();
                     InputStreamReader isr = new InputStreamReader(is);
                     BufferedReader reader = new BufferedReader(isr)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (AbstractResourceAccessor
                                .getResources(path + "/" + line).isEmpty()) {
                            // 无法从线程上下文ClassLoader获取资源
                            // 说明这个reader
                            return Collections.emptyList();
                        }
                        LOG.debugIfEnable("Reader entry: " + line);
                        children.add(line);
                    }
                } catch (InvalidPathException e) {
                    return Collections.emptyList();
                }
                return children;
            }
        } catch (FileNotFoundException e) {
            // 对于 URL#openStream() 调用可能会失败，
            // 具体取决于一些 servlet 容器, 例如Tomcat
            // Tomcat会限制读取文件夹而抛出FileNotFoundException
            if (!"file".equals(url.getProtocol())) {
                // 无从应对
                throw e;
            }
            File file = new File(url.getFile());
            if (!file.isDirectory()) {
                // 是文件而不是目录
                return Collections.emptyList();
            }
            LOG.debugIfEnable("Listing directory " + file.getAbsolutePath());
            LOG.debugIfEnable("Listing " + url);
            // 如果因为无法打开目录进行读取, 则直接列出目录.
            String[] list = file.list();
            if (list == null) {
                return Collections.emptyList();
            }
            return Arrays.asList(list);
        }
    }


    /**
     * @see #isJar(URL, byte[])
     */
    protected static boolean isJar(URL url) {
        return isJar(url, new byte[JAR_MAGIC.length]);
    }

    /**
     * 通过打开文件, 并检验魔数来确定是不是Jar包
     *
     * @param url    将被检验的资源的Url
     * @param buffer 缓冲区，从中读取资源的前面几个字节(魔数部分)。<br>
     *               缓冲区的大小必须至少为{@link #JAR_MAGIC}. <br>
     *               (为了优化，同一缓冲区可以重复利用, 所以就作为参数脱离本方法)
     * @return 如果是Jar包就返回true
     */
    protected static boolean isJar(URL url, byte[] buffer) {
        try (InputStream is = url.openStream()) {
            // 获取魔数的部分
            is.read(buffer, 0, JAR_MAGIC.length);
            if (Arrays.equals(buffer, JAR_MAGIC)) {
                // 相同就是Jar
                LOG.debugIfEnable("Found JAR: " + url);
                return true;
            }
            // 魔数的部分不相同, 就不是Jar包
        } catch (Exception e) {
            // 不能从这个流读取数据, 就说明这个文件就不是Jar包
        }

        return false;
    }


}
