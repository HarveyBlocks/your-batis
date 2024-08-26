package org.harvey.batis.scripting.xml;

import org.harvey.batis.builder.BaseBuilder;
import org.harvey.batis.config.Configuration;
import org.harvey.batis.exception.builder.BuilderException;
import org.harvey.batis.mapping.sqlsource.DynamicSqlSource;
import org.harvey.batis.mapping.sqlsource.RawSqlSource;
import org.harvey.batis.mapping.sqlsource.SqlSource;
import org.harvey.batis.parsing.XNode;
import org.harvey.batis.scripting.xml.handler.*;
import org.harvey.batis.scripting.xml.node.MixedSqlNode;
import org.harvey.batis.scripting.xml.node.StaticTextSqlNode;
import org.harvey.batis.scripting.xml.node.TextSqlNode;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO
 * 和{@link NodeHandler}(的实现类)一同组成了策略模式
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-11 17:57
 */
public class XmlScriptBuilder extends BaseBuilder {

    private final XNode context;
    private boolean isDynamic;
    private final Class<?> parameterType;
    private final Map<String, NodeHandler> nodeHandlerMap = new HashMap<>();

    public XmlScriptBuilder(Configuration configuration, XNode context) {
        this(configuration, context, null);
    }


    public XmlScriptBuilder(Configuration configuration, XNode context, Class<?> parameterType) {
        super(configuration);
        this.context = context;
        this.parameterType = parameterType;
        this.initNodeHandlerMap();
    }

    private void initNodeHandlerMap() {
        nodeHandlerMap.put(ChooseHandler.NODE_NAME, new ChooseHandler(configuration, this));
        nodeHandlerMap.put(ForEachHandler.NODE_NAME, new ForEachHandler(configuration, this));
        nodeHandlerMap.put(IfHandler.NODE_NAME, new IfHandler(configuration, this));
        nodeHandlerMap.put(WhenHandler.NODE_NAME, new WhenHandler(configuration, this));
        nodeHandlerMap.put(OtherwiseHandler.NODE_NAME, new OtherwiseHandler(configuration, this));
        nodeHandlerMap.put(SetHandler.NODE_NAME, new SetHandler(configuration, this));
        nodeHandlerMap.put(TrimHandler.NODE_NAME, new TrimHandler(configuration, this));
        nodeHandlerMap.put(WhereHandler.NODE_NAME, new WhereHandler(configuration, this));
    }

    protected NodeHandler getNodeHandler(String nodeName) {
        return nodeHandlerMap.get(nodeName);
    }

    public SqlSource parseScriptNode() {
        // 解析当前SQL语句
        MixedSqlNode rootSqlNode = this.parseDynamicTags(context);
        return this.isDynamic ? new DynamicSqlSource(configuration, rootSqlNode) :
                new RawSqlSource(configuration, rootSqlNode, parameterType);
    }

    /**
     * TODO
     * 其他嵌套的Node也可以调用该方法来解析
     *
     * @param node 原始的节点
     * @return 当前节点经过层层剖析之后获取的{@link MixedSqlNode}
     */
    protected MixedSqlNode parseDynamicTags(XNode node) {
        // 存放所有孩子节点
        // 例如
        //    <choose>
        //        <when match="1">when1_0<if>x<if></when>
        //        <when match="1">when1_1</when>
        //        <otherwise>default</otherwise>
        //    </choose>
        // 当前XNode是choose
        // 那么contents为choose: [when: ["when1_0", if: "x"],when: ["when1_1"], otherwise: ["default"]]
        // 是嵌套的
        // 当然每一层都是同一辈分的节点
        // 文本也被包装为SQLNode, 也是节点
        List<SqlNode> contents = new ArrayList<>();
        NodeList children = node.getNode().getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            XNode child = node.newOne(children.item(i));
            short nodeType = child.getNode().getNodeType();
            if (nodeType == Node.CDATA_SECTION_NODE ||
                    nodeType == Node.TEXT_NODE) {
                // 是文本
                contents.add(phaseTextNode(child));
            } else if (nodeType == Node.ELEMENT_NODE) { // issue #628
                phaseElementNode(child, contents);
                // 在解析任何一层的子节点中, 单反出现了Dynamic的Text节点
                // 或者出现了Element节点, 都认为这整个SQL节点是Dynamic的
                this.isDynamic = true;
            }
        }
        // 递归出口
        return new MixedSqlNode(contents);
    }

    /**
     * 解析Text节点
     */
    private SqlNode phaseTextNode(XNode child) {
        String body = child.getBody();
        String data = body == null ? "" : body;
        TextSqlNode textSqlNode = new TextSqlNode(data);
        if (textSqlNode.isDynamic()) {
            // 但是, 是动态SQL的文本

            // 在解析任何一层的子节点中, 单反出现了Dynamic的Text节点
            // 或者出现了Element节点, 都认为这整个SQL节点是Dynamic的
            this.isDynamic = true;
            return textSqlNode;
        } else {
            // 是纯文本
            return new StaticTextSqlNode(data);
        }
    }

    /**
     * 解析Element节点
     *
     * @param contents {@link MixedSqlNode}
     */
    private void phaseElementNode(XNode child, List<SqlNode> contents) {
        // 是子节点
        String nodeName = child.getNode().getNodeName();
        // 通过多态来决定执行的逻辑
        NodeHandler handler = this.nodeHandlerMap.get(nodeName);
        if (handler == null) {
            throw new BuilderException("Unknown element <" + nodeName + "> in SQL statement.");
        }
        // 递归, 解析嵌套的Child
        handler.handleNode(child, contents);
    }
}
