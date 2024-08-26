package org.harvey.batis.executor.param;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * TODO
 * 设置{@link java.sql.PreparedStatement}的参数
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-19 22:34
 */
public interface ParameterHandler {

    Object getParameterObject();

    void setParameters(PreparedStatement ps) throws SQLException;

}