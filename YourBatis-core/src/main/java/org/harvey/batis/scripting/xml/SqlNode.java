package org.harvey.batis.scripting.xml;


/**
 * TODO
 * 每一个SQL节点, 都能用{@link #apply(DynamicContext)}拼装SQL语句
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-11 23:29
 */
public interface SqlNode {
    /**
     * 构建上下文, 在这里真正将当前节点拼入上下文, 以此拼装SQL语句
     *
     * @param context 被构建的上下文, 目的是拼装SQL语句
     * @return 如果是If的match为false或choose的所有分支都没有命中, 返回false<br>
     * 其余情况返回true
     */
    boolean apply(DynamicContext context);
}
