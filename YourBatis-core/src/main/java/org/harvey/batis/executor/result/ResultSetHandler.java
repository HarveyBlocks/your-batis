package org.harvey.batis.executor.result;

import org.harvey.batis.cursor.Cursor;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-19 23:41
 */
public interface ResultSetHandler {
    <E> List<E> handleResultSets(Statement stmt) throws SQLException;

    <E> Cursor<E> handleCursorResultSets(Statement stmt) throws SQLException;

    void handleOutputParameters(CallableStatement cs) throws SQLException;



}
