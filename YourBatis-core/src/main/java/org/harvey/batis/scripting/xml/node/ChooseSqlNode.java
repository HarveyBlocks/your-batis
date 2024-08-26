package org.harvey.batis.scripting.xml.node;

import org.harvey.batis.config.Configuration;
import org.harvey.batis.parsing.GenericTokenParser;
import org.harvey.batis.scripting.xml.DynamicContext;
import org.harvey.batis.scripting.xml.SqlNode;

import java.util.List;
import java.util.Map;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-12 23:09
 */
public class ChooseSqlNode implements SqlNode {
    /**
     * 所有when节点解析后的SqlNode组成的集合<br>
     * 实质上是{@link IfSqlNode}
     */
    private final List<SqlNode> whenSqlNodes;
    /**
     * otherwise节点解析后的SqlNode
     */
    private final SqlNode defaultSqlNode;

    /**
     * 由于Choose只能选择两个子节点When和otherwise<br>
     * 需要的是解析后的两个节点的集合
     *
     * @param whenSqlNodes   {@link #whenSqlNodes}
     * @param defaultSqlNode {@link #defaultSqlNode}
     */
    public ChooseSqlNode(List<SqlNode> whenSqlNodes, SqlNode defaultSqlNode) {
        this.whenSqlNodes = whenSqlNodes;
        this.defaultSqlNode = defaultSqlNode;
    }

    @Override
    public boolean apply(DynamicContext context) {
        // cases
        for (SqlNode sqlNode : whenSqlNodes) {
            if (sqlNode.apply(context)) {
                // switch-case的break
                return true;
            }
        }

        // default
        if (defaultSqlNode != null) {
            defaultSqlNode.apply(context);
            return true;
        }
        return false;
    }


}
