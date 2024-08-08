package org.harvey.batis.builder.xml;

import org.harvey.batis.io.Resources;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * XML实例解析器
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-04 17:34
 */
public class XMLMapperEntityResolver implements EntityResolver {

    private static final String XSD_DICTIONARY_PATH = "org/harvey/batis/builder/xml/xsd/";
    private static final String YOURBATIS_CONFIG_SYSTEM = "yourbatis-config.xsd";
    private static final String YOURBATIS_MAPPER_SYSTEM = "yourbatis-mapper.xsd";
    private static final String YOURBATIS_CONFIG_XSD = XSD_DICTIONARY_PATH + YOURBATIS_CONFIG_SYSTEM;
    private static final String YOURBATIS_MAPPER_XSD = XSD_DICTIONARY_PATH + YOURBATIS_MAPPER_SYSTEM;

    public XMLMapperEntityResolver() {
    }

    /**
     * 解析XML实例
     *
     * @param publicId 被引用的外部实体的公共标识符，如果未提供任何信息，则为 null。
     * @param systemId 被引用的外部实体的系统标识符
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
        if (systemId == null) {
            return null;
        }
        String lowerCaseSystemId = systemId.toLowerCase(Locale.ENGLISH);
        String xsdFileName;
        // 判断是用哪个XSD文件解析这个XML
        if (lowerCaseSystemId.contains(YOURBATIS_CONFIG_SYSTEM)) {
            xsdFileName = YOURBATIS_CONFIG_XSD;
        } else if (lowerCaseSystemId.contains(YOURBATIS_MAPPER_SYSTEM)) {
            xsdFileName = YOURBATIS_MAPPER_XSD;
        } else {
            // 不含有? 那再见
            return null;
        }
        try {
            return getInputSource(xsdFileName, publicId, systemId);
        } catch (Exception e) {
            throw new SAXException(e.toString());
        }
    }

    /**
     * 依据path,获取source并返回
     * @param path schema的文件地址
     */
    private InputSource getInputSource(String path, String publicId, String systemId) {
        InputSource source = null;
        if (path == null) {
            return null;
        }
        try {
            InputStream in = Resources.getResourceAsStream(path);
            source = new InputSource(in);
            source.setPublicId(publicId);
            source.setSystemId(systemId);
        } catch (IOException e) {
            // ignore, null is ok
        }
        return source;
    }

}
