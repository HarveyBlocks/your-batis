package org.harvey.batis.scripting.xml.node;

import org.harvey.batis.config.Configuration;
import org.harvey.batis.scripting.js.ExpressionEvaluator;
import org.harvey.batis.scripting.xml.DynamicContext;
import org.harvey.batis.scripting.xml.SqlNode;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-13 00:00
 */
public class IfSqlNode extends DynamicSqlNode {

    private final String boolExpression;

    public IfSqlNode(Configuration configuration, SqlNode contents, String boolExpression) {
        super(configuration, contents);
        this.boolExpression = boolExpression;
    }

    @Override
    public boolean apply(DynamicContext context) {
        if (!ExpressionEvaluator.evaluateBoolean(boolExpression, context.getBindings())) {
            // if内部不解析
            return false;
        }
        // if内部进行解析
        contents.apply(context);
        return true;
    }
}
