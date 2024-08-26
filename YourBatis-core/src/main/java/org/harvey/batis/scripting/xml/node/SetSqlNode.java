package org.harvey.batis.scripting.xml.node;

import org.harvey.batis.config.Configuration;
import org.harvey.batis.scripting.xml.SqlNode;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * TODO
 * {@inheritDoc}
 * 继承TrimSqlNode<br>
 * 对字符串加上前缀"SET",  然后如果有","成为了前缀或后缀, 去除它
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-12 22:24
 */
public class SetSqlNode extends TrimSqlNode {

    /**
     * 只能获取一个","的集合
     */
    private static final List<String> COMMA = Collections.singletonList(",");

    /**
     * {@inheritDoc}
     */
    public SetSqlNode(Configuration configuration, SqlNode contents) {
        super(configuration, contents, "SET", COMMA, null, COMMA);
    }
}

