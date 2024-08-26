package org.harvey.batis.scripting.xml.handler;

import org.harvey.batis.config.Configuration;
import org.harvey.batis.parsing.MapperXmlConstants;
import org.harvey.batis.parsing.XNode;
import org.harvey.batis.scripting.xml.AbstractNodeHandler;
import org.harvey.batis.scripting.xml.SqlNode;
import org.harvey.batis.scripting.xml.XmlScriptBuilder;
import org.harvey.batis.scripting.xml.node.MixedSqlNode;
import org.harvey.batis.scripting.xml.node.SetSqlNode;

import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-12 17:12
 */
public  class SetHandler extends AbstractNodeHandler {
    public static final String NODE_NAME = MapperXmlConstants.DynamicSql.SET_ELEMENT;

    public SetHandler(Configuration configuration, XmlScriptBuilder key) {
        super(configuration, key);
        // Prevent Synthetic Access
    }

    @Override
    public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
        // 递归, 解析当前节点的孩子们
        MixedSqlNode mixedSqlNode = parseDynamicTags(nodeToHandle);
        SetSqlNode set = new SetSqlNode(configuration, mixedSqlNode);
        targetContents.add(set);
    }
}
