package org.harvey.batis.util.enums;

import java.sql.ResultSet;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-21 00:33
 */
public enum ResultSetType {
    /**
     * 与未设置相同(取决于 driver)
     */
    DEFAULT(-1),
    FORWARD_ONLY(ResultSet.TYPE_FORWARD_ONLY),
    SCROLL_INSENSITIVE(ResultSet.TYPE_SCROLL_INSENSITIVE),
    SCROLL_SENSITIVE(ResultSet.TYPE_SCROLL_SENSITIVE);

    private final int value;

    ResultSetType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static int value(ResultSetType type) {
        return type.value;
    }
}
