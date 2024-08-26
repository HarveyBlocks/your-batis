package org.harvey.batis.scripting.xml;

import org.harvey.batis.config.Configuration;
import org.harvey.batis.parsing.XNode;
import org.harvey.batis.scripting.xml.node.MixedSqlNode;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-11 23:42
 */
public abstract class AbstractNodeHandler implements NodeHandler {
    private final XmlScriptBuilder builder;
    protected final Configuration configuration;

    protected AbstractNodeHandler(Configuration configuration, XmlScriptBuilder key) {
        this.builder = key;
        this.configuration = configuration;
    }

    /**
     * @see XmlScriptBuilder#parseDynamicTags(XNode)
     */
    protected MixedSqlNode parseDynamicTags(XNode nodeToHandle) {
        return this.builder.parseDynamicTags(nodeToHandle);
    }

    /**
     * @see XmlScriptBuilder#getNodeHandler(String)
     */
    protected NodeHandler getNodeHandler(String nodeName) {
        return this.builder.getNodeHandler(nodeName);
    }
}
