package org.harvey.batis.io.log.jdbc;

import org.harvey.batis.io.log.Log;
import org.harvey.batis.util.ReflectionExceptionUnwrappedMaker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-21 21:23
 */
public class ResultSetLogger extends BaseJdbcLogger implements InvocationHandler {
    /**
     * BLOB斑点
     *
     * @see Types
     * @see Types#BINARY
     * @see Types#BLOB
     * @see Types#CLOB
     * @see Types#LONGNVARCHAR
     * @see Types#LONGVARBINARY
     * @see Types#LONGVARCHAR
     * @see Types#NCLOB
     * @see Types#VARBINARY
     */
    private static final Set<Integer> BLOB_TYPES = new HashSet<>();
    private final Set<Integer> blobColumns = new HashSet<>();
    private boolean first = true;
    /**
     * 行
     */
    private int rows;
    private final ResultSet rs;


    static {
        BLOB_TYPES.add(Types.BINARY);
        BLOB_TYPES.add(Types.BLOB);
        BLOB_TYPES.add(Types.CLOB);
        BLOB_TYPES.add(Types.LONGNVARCHAR);
        BLOB_TYPES.add(Types.LONGVARBINARY);
        BLOB_TYPES.add(Types.LONGVARCHAR);
        BLOB_TYPES.add(Types.NCLOB);
        BLOB_TYPES.add(Types.VARBINARY);
    }

    private ResultSetLogger(ResultSet rs, Log statementLog, int queryStack) {
        super(statementLog, queryStack);
        this.rs = rs;
    }

    /**
     * @see ResultSetLogger#ResultSetLogger(ResultSet, Log, int)
     */
    public static ResultSet newInstance(ResultSet rs, Log statementLog, int queryStack) {
        InvocationHandler handler = new ResultSetLogger(rs, statementLog, queryStack);
        ClassLoader cl = ResultSet.class.getClassLoader();
        return (ResultSet) Proxy.newProxyInstance(cl, new Class[]{ResultSet.class}, handler);
    }

    /**
     * 对结果进行日志打印
     *
     * @see #postResultSetNext(Boolean)
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
        try {
            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, params);
            }
            Object result = method.invoke(rs, params);
            if ("next".equals(method.getName())) {
                this.postResultSetNext((Boolean) result);
            }
            super.clearColumnInfo();
            return result;
        } catch (Throwable t) {
            throw ReflectionExceptionUnwrappedMaker.unwrapThrowable(t);
        }
    }

    /**
     * next方法的post增强, 打印结果集的日志
     */
    private void postResultSetNext(Boolean hasNext) throws SQLException {
        if (hasNext) {
            rows++;
            this.traceResultSet();
        } else {
            // 结束了
            this.debug("     Total: " + rows, false);
        }
    }

    private void traceResultSet() throws SQLException {
        if (!isTraceEnabled()) {
            // TODO return;
        }
        ResultSetMetaData resultSetMetaData = rs.getMetaData();
        final int columnCount = resultSetMetaData.getColumnCount();
        if (first) {
            first = false;
            //  记录是BLOB_TYPES的位置
            this.printColumnHeaders(resultSetMetaData, columnCount);
        }
        // 记录后面Value的日志
        this.printColumnValues(columnCount);
    }

    /**
     * {@link #BLOB_TYPES}的, 只记录开头一遍
     */
    private void printColumnHeaders(ResultSetMetaData resultSetMetaData, int columnCount) throws SQLException {
        StringJoiner row = new StringJoiner(", ", "   Columns: ", "");
        for (int i = 1; i <= columnCount; i++) {
            if (BLOB_TYPES.contains(resultSetMetaData.getColumnType(i))) {
                // 是BLOB_TYPES的, 记录位置
                blobColumns.add(i);
            }
            row.add(resultSetMetaData.getColumnLabel(i));
        }
        trace(row.toString(), false);
        // System.out.println(row);
    }

    /**
     * {@link #BLOB_TYPES}的, 不再记录, 用{@code "<<BLOB>>"}标识
     */
    private void printColumnValues(int columnCount) {
        StringJoiner row = new StringJoiner(", ", "       Row: ", "");
        for (int i = 1; i <= columnCount; i++) {
            try {
                if (blobColumns.contains(i)) {
                    // 存在于blobColumns
                    row.add("<<BLOB>>");
                } else {
                    row.add(rs.getString(i));
                }
            } catch (SQLException e) {
                // generally can't call getString() on a BLOB column
                row.add("<<Cannot Display>>");
            }
        }
        this.trace(row.toString(), false);
        // System.out.println(row);
    }
}
