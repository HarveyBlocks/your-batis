package org.harvey.batis.io;

import org.harvey.batis.exception.UnfinishedFunctionException;
import org.harvey.batis.io.log.Log;
import org.harvey.batis.io.log.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarInputStream;

/**
 * TODO
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
     * TODO
     */
    @Override
    protected List<String> list(URL url, String path) throws IOException {
        InputStream is = null;
        try {
            List<String> resources;

            // First, try to find the URL of a JAR file containing the requested resource. If a JAR
            // file is found, then we'll list child resources by reading the JAR.
            // 尝试查找包含所请求资源的 JAR 文件的 URL。
            // 如果找到 JAR 文件，我们将通过读取 JAR 来列出子资源。
            URL jarUrl = this.findJarForResource(url);
            if (jarUrl != null) {
                // 是Jar的URL, 那么此时
                // jarUrl是Jar包的地址
                // path是jar内的文件的路径
                is = jarUrl.openStream();
                // is是Jar包的流
                LOG.debugIfEnable("Listing " + url);
                resources = this.listResources(new JarInputStream(is), path);
            } else {
                // TODO
                throw new UnfinishedFunctionException();
            }
            return resources;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

    /**
     * TODO 从Jar包中获取path资源
     * path的资源一定在Jar包中
     *
     * @param jarInputStream 指定jar包的InputStream
     * @param path           jar包中的资源的路径(相对路径)
     * @return path指向的在jar包中的资源(指path本身下的所有资源及其所有子包下的所有资源)
     */
    private List<String> listResources(JarInputStream jarInputStream, String path) {
        throw new UnfinishedFunctionException(jarInputStream, path);
    }


    /**
     * 解构给定的 URL 查找包含 URL 引用的资源的 JAR 文件.<br>
     * 假设 URL 中有引用 JAR, 返回 JAR 文件本身的 URL.<br>
     * 如果无法找到 JAR，则返回 null<br>
     *
     * @param url 如果指向Jar包内的文件, 是这样的:
     *            <pre>{@code "jar:file:/D:/IT_study/maven/repository/ch/qos/logback/logback-core/1.2.3/logback-core-1.2.3.jar!/ch/qos/logback/core"}</pre>
     * @return 如果不是Jar包, 返回null, 是就返回Jar包的地址URL
     */
    protected URL findJarForResource(URL url) {
        LOG.debugIfEnable("Find JAR URL: " + url);
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
                String file = url.getFile();
                url = new URL(file);
                LOG.debugIfEnable("Inner URL: " + url);
            } catch (MalformedURLException e) {
                // MalformedURLException 表示发生了格式错误的 URL
                // 在字符串中找不到有效的协议，或者无法解析该字符串。
                // 不正常说明url的file部分就是文件路径了
                break;
            }
        }
        // URL#toExternalForm == URL#toString
        StringBuilder jarUrlStringBuilder = new StringBuilder(url.toExternalForm());
        // 获取.jar后缀的位置
        int index = jarUrlStringBuilder.lastIndexOf(".jar");
        if (index < 0) {
            // 没有.jar的结尾, 直接认定不是jar包
            LOG.debugIfEnable("Not a JAR: " + jarUrlStringBuilder);
            return null;
        }
        // 有.jar的文件后缀
        // 提取Jar的URL(去除jar包路径后面的内容)
        // logback-core-1.2.3.jar!/ch/qos/logback/core -> logback-core-1.2.3.jar
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
