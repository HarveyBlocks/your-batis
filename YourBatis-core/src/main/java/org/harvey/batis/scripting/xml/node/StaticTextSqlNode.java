package org.harvey.batis.scripting.xml.node;

import org.harvey.batis.scripting.xml.DynamicContext;
import org.harvey.batis.scripting.xml.SqlNode;

/**
 * TODO
 * 绝对简单的文本类, 不需要解析${}的部分
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-12 00:08
 */
public class StaticTextSqlNode implements SqlNode {
    private final String text;

    public StaticTextSqlNode(String text) {
        this.text = text;
    }

    /**
     * @param context 被构建的上下文, 目的是拼装SQL语句
     * @return true
     * @see DynamicContext#appendSql(String)
     */
    @Override
    public boolean apply(DynamicContext context) {
        // 去除头尾空白符
        context.appendSql(text);
        return true;
    }
}
