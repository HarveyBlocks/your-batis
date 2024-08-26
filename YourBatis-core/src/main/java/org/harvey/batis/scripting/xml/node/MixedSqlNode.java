package org.harvey.batis.scripting.xml.node;

import org.harvey.batis.scripting.xml.DynamicContext;
import org.harvey.batis.scripting.xml.SqlNode;

import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-11 23:55
 * @see #contents
 * @see #apply(DynamicContext)
 */
public class MixedSqlNode implements SqlNode {
    /**
     * 存放孩子节点<br>
     * 这些孩子都是同一层的节点(只有儿子辈, 没有孙子, 及后代辈)<br>
     * 有些节点也是{@link MixedSqlNode}, 那么也会有这个字段<br>
     * 那么也会存放它们的同一层孩子节点
     */
    private final List<SqlNode> contents;

    /**
     * @param contents {@link #contents}
     */
    public MixedSqlNode(List<SqlNode> contents) {
        this.contents = contents;
    }


    /**
     * {@inheritDoc}
     * <p>
     * 调用{@link #contents}里所有的节点内容, 拼装各自的内容
     *
     * @param context 被构建的上下文, 目的是拼装SQL语句
     * @return
     */
    @Override
    public boolean apply(DynamicContext context) {
        contents.forEach(node -> node.apply(context));
        return true;
    }
}
