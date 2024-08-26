package org.harvey.batis.scripting.xml.handler;

import org.harvey.batis.config.Configuration;
import org.harvey.batis.parsing.MapperXmlConstants;
import org.harvey.batis.scripting.xml.XmlScriptBuilder;

/**
 * 用when的标识生成IfHandler
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-12 23:48
 */
public class WhenHandler extends IfHandler{
    public static final String NODE_NAME = MapperXmlConstants.DynamicSql.WHEN_ELEMENT;

    public WhenHandler(Configuration configuration, XmlScriptBuilder key) {
        super(configuration, key);
    }
}
