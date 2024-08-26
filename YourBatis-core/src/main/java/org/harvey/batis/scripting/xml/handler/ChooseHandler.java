package org.harvey.batis.scripting.xml.handler;

import org.harvey.batis.config.Configuration;
import org.harvey.batis.exception.builder.BuilderException;
import org.harvey.batis.parsing.MapperXmlConstants;
import org.harvey.batis.parsing.XNode;
import org.harvey.batis.scripting.xml.AbstractNodeHandler;
import org.harvey.batis.scripting.xml.NodeHandler;
import org.harvey.batis.scripting.xml.SqlNode;
import org.harvey.batis.scripting.xml.XmlScriptBuilder;
import org.harvey.batis.scripting.xml.node.ChooseSqlNode;
import org.harvey.batis.util.OneGetter;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-12 17:14
 */
public class ChooseHandler extends AbstractNodeHandler {

    public static final String NODE_NAME = MapperXmlConstants.DynamicSql.CHOOSE_ELEMENT;

    public ChooseHandler(Configuration configuration, XmlScriptBuilder key) {
        super(configuration, key);
        // Prevent Synthetic Access
    }

    /**
     * 由于Choose只能选择两个子节点When和otherwise<br>
     * 所以只解析这两个节点的集合
     *
     * @param nodeToHandle   节点本身
     * @param targetContents 前面的节点, <br>
     *                       也有一些不是节点, 是单纯的文本, 但看作节点<br>
     *                       和当前待解析的节点是同一层的
     * @see #getDefaultSqlNode(List)
     * @see #parseDynamicTags(XNode)
     * @see ChooseSqlNode
     */
    @Override
    public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
        // 如果子节点是when/if, 则携带对应的content
        List<SqlNode> whenSqlNodes = new ArrayList<>();
        // 如果子节点是otherwise, 则携带otherwise特有的content
        List<SqlNode> otherwiseSqlNodes = new ArrayList<>();
        List<XNode> children = nodeToHandle.getChildren();
        for (XNode child : children) {
            String nodeName = child.getNode().getNodeName();
            NodeHandler handler = super.getNodeHandler(nodeName);
            if (handler instanceof IfHandler) {
                handler.handleNode(child, whenSqlNodes);
            } else if (handler instanceof OtherwiseHandler) {
                handler.handleNode(child, otherwiseSqlNodes);
            } else {
                throw new BuilderException("Unknown element <" + nodeName + "> in choose statement.");
            }
        }

        SqlNode defaultSqlNode = getDefaultSqlNode(otherwiseSqlNodes);
        ChooseSqlNode chooseSqlNode = new ChooseSqlNode(whenSqlNodes, defaultSqlNode);
        targetContents.add(chooseSqlNode);
    }


    /**
     * 确保只有一个defaultSqlNode
     *
     * @throws BuilderException 如果不止一个, 就抛出异常
     */
    private SqlNode getDefaultSqlNode(List<SqlNode> defaultSqlNodes) {
        return new OneGetter<SqlNode>("default (otherwise) elements", "choose statement")
                .one(defaultSqlNodes);
    }
}