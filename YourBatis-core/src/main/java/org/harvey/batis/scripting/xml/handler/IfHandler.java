package org.harvey.batis.scripting.xml.handler;

import org.harvey.batis.config.Configuration;
import org.harvey.batis.parsing.MapperXmlConstants;
import org.harvey.batis.parsing.XNode;
import org.harvey.batis.scripting.xml.AbstractNodeHandler;
import org.harvey.batis.scripting.xml.SqlNode;
import org.harvey.batis.scripting.xml.XmlScriptBuilder;
import org.harvey.batis.scripting.xml.node.IfSqlNode;
import org.harvey.batis.scripting.xml.node.MixedSqlNode;

import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-12 17:15
 */
public class IfHandler extends AbstractNodeHandler {
    public static final String NODE_NAME = MapperXmlConstants.DynamicSql.IF_ELEMENT;
    private static final String MATCH_ATTRIBUTION = MapperXmlConstants.DynamicSql.MATCH_ATTRIBUTION;

    public IfHandler(Configuration configuration, XmlScriptBuilder key) {
        super(configuration, key);
        // Prevent Synthetic Access
    }

    @Override
    public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
        MixedSqlNode mixedSqlNode = super.parseDynamicTags(nodeToHandle);
        String match = nodeToHandle.getAttributeValue(MATCH_ATTRIBUTION);
        IfSqlNode ifSqlNode = new IfSqlNode(configuration, mixedSqlNode, match);
        targetContents.add(ifSqlNode);
    }
}
