package org.harvey.batis.util.enums;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-23 23:21
 */
/**
 * Specifies if and how MyBatis should automatically map columns to fields/properties.
 *
 * @author Eduardo Macarron
 */
public enum AutoMappingBehavior {

    /**
     * 不启用 auto-mapping.
     */
    NONE,

    /**
     * 未定义嵌套ResultMap, 则启用auto-mapping
     */
    PARTIAL,

    /**
     * 总是开启auto-mapping
     */
    FULL
}
