package org.harvey.batis.scripting.xml.handler;

import org.harvey.batis.config.Configuration;
import org.harvey.batis.parsing.MapperXmlConstants;
import org.harvey.batis.parsing.XNode;
import org.harvey.batis.scripting.xml.AbstractNodeHandler;
import org.harvey.batis.scripting.xml.SqlNode;
import org.harvey.batis.scripting.xml.XmlScriptBuilder;
import org.harvey.batis.scripting.xml.node.ForEachSqlNode;
import org.harvey.batis.scripting.xml.node.MixedSqlNode;

import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-12 17:15
 */
public class ForEachHandler extends AbstractNodeHandler {
    public static final String NODE_NAME = MapperXmlConstants.DynamicSql.FOREACH_ELEMENT;
    private static final String COLLECTION_ATTRIBUTION = MapperXmlConstants.DynamicSql.COLLECTION_ATTRIBUTION;
    private static final String ITEM_ATTRIBUTION = MapperXmlConstants.DynamicSql.ITEM_ATTRIBUTION;
    private static final String INDEX_ATTRIBUTION = MapperXmlConstants.DynamicSql.INDEX_ATTRIBUTION;
    private static final String OPEN_ATTRIBUTION = MapperXmlConstants.DynamicSql.OPEN_ATTRIBUTION;
    private static final String CLOSE_ATTRIBUTION = MapperXmlConstants.DynamicSql.CLOSE_ATTRIBUTION;
    private static final String SEPARATOR_ATTRIBUTION = MapperXmlConstants.DynamicSql.SEPARATOR_ATTRIBUTION;

    public ForEachHandler(Configuration configuration, XmlScriptBuilder key) {
        super(configuration, key);
        // Prevent Synthetic Access
    }

    @Override
    public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
        // 递归, 解析当前节点的孩子们
        MixedSqlNode mixedSqlNode = super.parseDynamicTags(nodeToHandle);
        String collection = nodeToHandle.getAttributeValue(COLLECTION_ATTRIBUTION);
        String item = nodeToHandle.getAttributeValue(ITEM_ATTRIBUTION);
        String index = nodeToHandle.getAttributeValue(INDEX_ATTRIBUTION);
        String open = nodeToHandle.getAttributeValue(OPEN_ATTRIBUTION);
        String close = nodeToHandle.getAttributeValue(CLOSE_ATTRIBUTION);
        String separator = nodeToHandle.getAttributeValue(SEPARATOR_ATTRIBUTION);
        ForEachSqlNode foreach = new ForEachSqlNode(
                configuration, mixedSqlNode,
                collection, item, index,
                open, close, separator);
        targetContents.add(foreach);
    }
}