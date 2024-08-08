package org.harvey.batis.parsing;

import lombok.Getter;
import org.w3c.dom.CharacterData;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Node的包装类
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-05 16:14
 * @see Node
 */
public class XNode {
    /**
     * 被封装的节点
     */
    private final Node node;
    @Getter
    private final String name;
    private final String body;
    @Getter
    private final Properties attributes;
    /**
     * @see PropertyParser#parse(String, Properties)
     */
    private final Properties variables;
    /**
     * TODO 为什么一个节点连解析器都需要啊?
     */
    private final XPathParser xpathParser;

    /**
     * @param xpathParser {@link #xpathParser}
     * @param node        {@link #node}
     * @param variables   {@link #variables}
     */
    public XNode(XPathParser xpathParser, Node node, Properties variables) {
        this.xpathParser = xpathParser;
        this.variables = variables;
        this.node = node;
        this.name = node.getNodeName();
        this.attributes = this.parseAttributes(node);
        this.body = this.parseBody(node);
    }

    /**
     * 从child获取属性, 将文本的引用内容替换成实际内容<br>
     * 引用内容和实际内容的映射存放在variable
     *
     * @see PropertyParser#parse(String, Properties)
     */
    private Properties parseAttributes(Node n) {
        Properties attributes = new Properties();
        NamedNodeMap attributeNodes = n.getAttributes();
        if (attributeNodes == null) {
            return attributes;
        }
        for (int i = 0; i < attributeNodes.getLength(); i++) {
            Node attribute = attributeNodes.item(i);
            // 遍历attributeNodes
            // 将属性的值中存在的引用内容替换成实际内容
            // 引用内容和实际内容的映射存放在variables
            String value = PropertyParser.parse(attribute.getNodeValue(), variables);
            attributes.put(attribute.getNodeName(), value);
        }
        return attributes;
    }

    /**
     * 从node中({@link #getBodyData(Node)})获取data, 然后返回
     */
    private String parseBody(Node node) {
        String data = this.getBodyData(node);
        if (data != null) {
            return data;
        }
        NodeList children = node.getChildNodes();
        for (int i = 0; data == null && i < children.getLength(); i++) {
            Node child = children.item(i);
            // 不断尝试从child中获取data
            data = this.getBodyData(child);
            // 直到获取到了data
            // 啊?一个Child的Data就够了?
        }
        return data;
    }

    /**
     * 如果child不是嵌套类型(是就返回null)<br>
     * 从child获取文本, 将文本的引用内容替换成实际内容<br>
     * 引用内容和实际内容的映射存放在variable
     *
     * @see PropertyParser#parse(String, Properties)
     */
    private String getBodyData(Node child) {
        if (child.getNodeType() != Node.CDATA_SECTION_NODE && child.getNodeType() != Node.TEXT_NODE) {
            // 是嵌套类型的Node(complexType)
            return null;
        }
        // 有内容的
        String data = ((CharacterData) child).getData();
        // 将文本的引用内容替换成实际内容
        // 引用内容和实际内容的映射存放在variables
        data = PropertyParser.parse(data, variables);
        return data;
    }

    public XNode evaluateNode(String expression) {
        return xpathParser.evaluateNode(node, expression);
    }

    /**
     * name --{@link #attributes}-> result
     */
    public String getAttributeValue(String name) {
        return this.getAttributeValue(name, null);
    }

    /**
     * name --{@link #attributes}-> result
     */
    public String getAttributeValue(String name, String defaultValue) {
        String value = attributes.getProperty(name);
        return value == null ? defaultValue : value;
    }

    public Properties getChildrenAsProperties() {
        Properties properties = new Properties();
        for (XNode child : getChildren()) {
            String name = child.name;
            String value = child.body;
            if (name != null && value != null) {
                properties.setProperty(name, value);
            }
        }
        return properties;
    }

    /**
     * @return {@link Node#getNodeType()}是{@link Node#ELEMENT_NODE}的,
     * 被封装成XNode, 装入结果集返回
     * @see Node#getChildNodes()
     */
    public List<XNode> getChildren() {
        List<XNode> children = new ArrayList<>(); // 结果集
        NodeList nodeList = node.getChildNodes();
        for (int i = 0, n = nodeList.getLength(); i < n; i++) {
            Node node = nodeList.item(i);
            // 遍历儿子们
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                // 是Node的儿子们封装成XNode, 存入结果集
                children.add(new XNode(xpathParser, node, variables));
            }
        }
        return children;
    }
}
