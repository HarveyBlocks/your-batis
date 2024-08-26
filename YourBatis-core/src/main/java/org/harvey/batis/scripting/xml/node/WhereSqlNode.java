package org.harvey.batis.scripting.xml.node;

import org.harvey.batis.config.Configuration;
import org.harvey.batis.exception.UnfinishedFunctionException;
import org.harvey.batis.scripting.xml.DynamicContext;
import org.harvey.batis.scripting.xml.SqlNode;

import java.util.Arrays;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-13 00:06
 */
public class WhereSqlNode extends TrimSqlNode {

    private static final List<String> PREFIX_LIST = Arrays.asList("AND ","OR ","AND\n", "OR\n", "AND\r", "OR\r", "AND\t", "OR\t");
    private static final String PREFIX = "WHERE";

    public WhereSqlNode(Configuration configuration, SqlNode contents) {
        super(configuration, contents,PREFIX , PREFIX_LIST, null, null);
    }

}
