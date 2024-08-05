package org.harvey.batis.builder.xml;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-04 17:34
 */
public class XMLMapperEntityResolver implements EntityResolver {
    public XMLMapperEntityResolver() {
    }

    /**
     * TODO
     *
     * @param publicId 被引用的外部实体的公共标识符，如果未提供任何信息，则为 null。
     * @param systemId 被引用的外部实体的系统标识符
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) {
        //  throw new UnfinishedFunctionException(publicId, systemId);
        return null;
    }
}
