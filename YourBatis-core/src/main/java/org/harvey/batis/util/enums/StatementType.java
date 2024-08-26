package org.harvey.batis.util.enums;

/**
 * 依据配置决定对Statement(xml中配置的SQL语句)解析的方式
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-13 01:09
 */
public enum StatementType {
    /**
     * 直接操作sql，不进行预编译，获取数据：$—Statement
     */
    STATEMENT,
    /**
     * 预处理，参数，进行预编译，获取数据：#—–PreparedStatement:默认
     */
    PREPARED,
    /**
     * 执行存储过程————CallableStatement
     */
    CALLABLE
}
