package org.harvey.batis.util.enums;


import lombok.Getter;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库类型{@link java.sql.JDBCType}
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-10 16:31
 */
@Getter
public enum JdbcType {
    /*
     * This is added to enable basic support for the
     * ARRAY data type - but a custom type handler is still required
     */
    ARRAY(Types.ARRAY),
    BIT(Types.BIT),
    TINYINT(Types.TINYINT),
    SMALLINT(Types.SMALLINT),
    INTEGER(Types.INTEGER),
    BIGINT(Types.BIGINT),
    FLOAT(Types.FLOAT),
    REAL(Types.REAL),
    DOUBLE(Types.DOUBLE),
    NUMERIC(Types.NUMERIC),
    DECIMAL(Types.DECIMAL),
    CHAR(Types.CHAR),
    VARCHAR(Types.VARCHAR),
    LONG_VARCHAR(Types.LONGVARCHAR),
    DATE(Types.DATE),
    TIME(Types.TIME),
    TIMESTAMP(Types.TIMESTAMP),
    BINARY(Types.BINARY),
    VARBINARY(Types.VARBINARY),
    LONG_VARBINARY(Types.LONGVARBINARY),
    NULL(Types.NULL),
    OTHER(Types.OTHER),
    BLOB(Types.BLOB),
    CLOB(Types.CLOB),
    BOOLEAN(Types.BOOLEAN),
    UNDEFINED(Integer.MIN_VALUE + 1000),
    NVARCHAR(Types.NVARCHAR), // JDK6
    NCHAR(Types.NCHAR), // JDK6
    NCLOB(Types.NCLOB), // JDK6
    STRUCT(Types.STRUCT),
    JAVA_OBJECT(Types.JAVA_OBJECT),
    DISTINCT(Types.DISTINCT),
    REF(Types.REF),
    DATA_LINK(Types.DATALINK),
    ROW_ID(Types.ROWID), // JDK6
    LONG_NVARCHAR(Types.LONGNVARCHAR), // JDK6
    SQL_XML(Types.SQLXML), // JDK6
    TIME_WITH_TIMEZONE(Types.TIME_WITH_TIMEZONE), // JDBC 4.2 JDK8
    TIMESTAMP_WITH_TIMEZONE(Types.TIMESTAMP_WITH_TIMEZONE); // JDBC 4.2 JDK8
    private final int typeCode;
    private static final Map<Integer, JdbcType> CODE_LOOKUP = new HashMap<>();

    static {
        for (JdbcType type : JdbcType.values()) {
            CODE_LOOKUP.put(type.typeCode, type);
        }
    }

    JdbcType(int code) {
        this.typeCode = code;
    }

    public static JdbcType forCode(int code) {
        return CODE_LOOKUP.get(code);
    }
}
