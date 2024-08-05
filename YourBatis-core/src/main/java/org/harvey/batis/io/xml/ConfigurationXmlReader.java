package org.harvey.batis.io.xml;

/**
 * TODO 配置文件, Xml的读取和解析器
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-24 11:11
 */
public class ConfigurationXmlReader {
    private String assembleUrl(String host, int port,
                               String database,
                               String serverTimezone,
                               boolean useSsl) {
        String file = "/" + database + "?" +
                "useSSL=" + useSsl + "&" +
                "serverTimezone=" + serverTimezone;
        return "jdbc:mysql:\\" + host + ":" + port + file;
    }
}
