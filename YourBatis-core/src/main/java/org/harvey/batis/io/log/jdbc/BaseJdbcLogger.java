package org.harvey.batis.io.log.jdbc;

import org.harvey.batis.builder.SqlSourceBuilder;
import org.harvey.batis.io.log.Log;
import org.harvey.batis.util.ArrayUtil;

import java.lang.reflect.Method;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-19 23:48
 */
public abstract class BaseJdbcLogger {

    /**
     * 以set打头的方法.
     * 第一个参数是key, 第二个参数是value, value将被写入到key里去
     */
    protected static final Set<String> SET_METHODS;
    /**
     * "execute"
     * "executeUpdate"
     * "executeQuery"
     * "addBatch"
     */
    protected static final Set<String> EXECUTE_METHODS = new HashSet<>();

    protected final Log statementLog;
    /**
     * TODO
     */
    private final Map<Object, Object> columnMap = new HashMap<>();

    /**
     * TODO
     */
    private final List<Object> columnNames = new ArrayList<>();
    /**
     * TODO
     */
    private final List<Object> columnValues = new ArrayList<>();
    protected final int queryStack;

    /*
     * Default constructor
     */
    public BaseJdbcLogger(Log log, int queryStack) {
        this.statementLog = log;
        if (queryStack == 0) {
            this.queryStack = 1;
        } else {
            this.queryStack = queryStack;
        }
    }

    static {
        SET_METHODS = Arrays.stream(PreparedStatement.class.getDeclaredMethods()).filter(method -> method.getName().startsWith("set")).filter(method -> method.getParameterCount() > 1)
                //  FieldProperties.isSetter 不好使
                .map(Method::getName).collect(Collectors.toSet());
        EXECUTE_METHODS.add("execute");
        EXECUTE_METHODS.add("executeUpdate");
        EXECUTE_METHODS.add("executeQuery");
        EXECUTE_METHODS.add("addBatch");
    }

    /**
     * @see #columnMap
     * @see #columnNames
     * @see #columnValues
     */
    protected void setColumn(Object key, Object value) {
        columnMap.put(key, value);
        columnNames.add(key);
        columnValues.add(value);
    }

    /**
     * @see #columnMap
     * @see #columnNames
     * @see #columnValues
     */
    protected void clearColumnInfo() {
        columnMap.clear();
        columnNames.clear();
        columnValues.clear();
    }

    /**
     * 形如<pre>{@code
     *  "12(Integer), null, 12(Integer), 12(Integer)"
     * }</pre>
     * @return {@link #columnValues}转成字符串
     * @see BaseJdbcLogger#objectValueString(Object)
     */
    protected String getParameterValueString() {
        List<Object> typeList = new ArrayList<>(columnValues.size());
        for (Object value : columnValues) {
            String string = value == null ? "null" :
                    BaseJdbcLogger.objectValueString(value) + "(" + value.getClass().getSimpleName() + ")";
            typeList.add(string);
        }
        final String parameters = typeList.toString();
        // 掐头去尾(ArrayList转字符串有[1,2,3,....]边框)
        return parameters.substring(1, parameters.length() - 1);
    }


    protected boolean isDebugEnabled() {
        return statementLog.isDebugEnabled();
    }

    protected boolean isTraceEnabled() {
        return statementLog.isTraceEnabled();
    }

    protected void debug(String text, boolean input) {
        statementLog.debug(prefix(input) + text);
    }

    protected void debugIfEnable(String text, boolean input) {
        if (isDebugEnabled()) {
            debug(text, input);
        }
    }

    protected void trace(String text, boolean input) {
        statementLog.trace(prefix(input) + text);
    }

    protected void traceIfEnable(String text, boolean input) {
        if (isTraceEnabled()) {
            trace(text, input);
        }
    }

    /**
     * <li>{@link #queryStack}=1, isInput=true<pre>{@code
     * "==> "}</pre></li>
     * <li>{@link #queryStack}=1, isInput=false<pre>{@code
     * "<== "}</pre></li>
     * <li>{@link #queryStack}=2, isInput=true<pre>{@code
     * "====> "}</pre></li>
     * <li>{@link #queryStack}=2, isInput=false<pre>{@code
     * "<==== "}</pre></li>
     * <li>{@link #queryStack}=3, isInput=true<pre>{@code
     * "======> "}</pre></li>
     * <li>{@link #queryStack}=3, isInput=false<pre>{@code
     * "<====== "}</pre></li>
     */
    private String prefix(boolean isInput) {
        char[] buffer = new char[queryStack * 2 + 2];
        // 填满=, 最后一个留空格
        Arrays.fill(buffer, '=');
        buffer[queryStack * 2 + 1] = ' ';
        if (isInput) {
            // 倒数第二个做>
            buffer[queryStack * 2] = '>';
        } else {
            // 第一个做<
            buffer[0] = '<';
        }
        return new String(buffer);
    }

    /**
     * @param original sql语句
     * @see SqlSourceBuilder#removeExtraWhitespaces(String)
     */
    protected static String removeExtraWhitespace(String original) {
        return SqlSourceBuilder.removeExtraWhitespaces(original);
    }

    /**
     * 将value转成String
     *
     * @see Array
     * @see Array#getArray()
     */
    protected static String objectValueString(Object value) {
        if (!(value instanceof Array)) {
            return value.toString();
        }
        try {
            return ArrayUtil.toString(((Array) value).getArray());
        } catch (SQLException e) {
            return value.toString();
        }
    }
}
