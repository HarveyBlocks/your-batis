package org.harvey.batis.scripting.xml.node;

import org.harvey.batis.config.Configuration;
import org.harvey.batis.scripting.xml.SqlNode;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-12 22:24
 */
public abstract class DynamicSqlNode implements SqlNode {

    protected final Configuration configuration;
    /**
     * 当前节点解析了子节点和子内容之后的节点
     */
    protected final SqlNode contents;

    /**
     * @param contents {@link #contents}
     */
    protected DynamicSqlNode(Configuration configuration, SqlNode contents) {
        this.configuration = configuration;
        this.contents = contents;
    }
}
