package org.harvey.batis.scripting.xml.handler;

import org.harvey.batis.config.Configuration;
import org.harvey.batis.parsing.MapperXmlConstants;
import org.harvey.batis.parsing.XNode;
import org.harvey.batis.scripting.xml.AbstractNodeHandler;
import org.harvey.batis.scripting.xml.SqlNode;
import org.harvey.batis.scripting.xml.XmlScriptBuilder;
import org.harvey.batis.scripting.xml.node.MixedSqlNode;
import org.harvey.batis.scripting.xml.node.TrimSqlNode;

import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-12 17:10
 */
public class TrimHandler extends AbstractNodeHandler {
    public static final String NODE_NAME = MapperXmlConstants.DynamicSql.TRIM_ELEMENT;
    private static final String PREFIX_ATTRIBUTION = MapperXmlConstants.DynamicSql.PREFIX_ATTRIBUTION;
    private static final String PREFIX_OVERRIDES_ATTRIBUTION = MapperXmlConstants.DynamicSql.PREFIX_OVERRIDES_ATTRIBUTION;
    private static final String SUFFIX_ATTRIBUTION = MapperXmlConstants.DynamicSql.SUFFIX_ATTRIBUTION;
    private static final String SUFFIX_OVERRIDES_ATTRIBUTION = MapperXmlConstants.DynamicSql.SUFFIX_OVERRIDES_ATTRIBUTION;

    public TrimHandler(Configuration configuration, XmlScriptBuilder key) {
        super(configuration, key);
        // Prevent Synthetic Access
    }

    /**
     * {@inheritDoc}
     *
     * @param nodeToHandle   trim节点本身
     * @param targetContents trim节点前面的节点, <br>
     *                       和当前待解析的节点是同一层的<br>
     * @see TrimSqlNode
     */
    @Override
    public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
        // 递归, 解析当前节点的孩子们
        MixedSqlNode mixedSqlNode = super.parseDynamicTags(nodeToHandle);
        String prefix = nodeToHandle.getAttributeValue(PREFIX_ATTRIBUTION);
        String prefixOverrides = nodeToHandle.getAttributeValue(PREFIX_OVERRIDES_ATTRIBUTION);
        String suffix = nodeToHandle.getAttributeValue(SUFFIX_ATTRIBUTION);
        String suffixOverrides = nodeToHandle.getAttributeValue(SUFFIX_OVERRIDES_ATTRIBUTION);
        TrimSqlNode trim = new TrimSqlNode(super.configuration, mixedSqlNode,
                prefix, prefixOverrides, suffix, suffixOverrides);
        targetContents.add(trim);
    }
}