package org.harvey.batis.util.enums;


/**
 * 相对于向XML的SQL语句填入数据, 就是OUT
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-13 18:12
 */
public enum ParameterMode {
    /**
     * 默认
     */
    IN,
    OUT,
    /**
     * 可读可写, 状态叠加
     */
    INOUT
}
