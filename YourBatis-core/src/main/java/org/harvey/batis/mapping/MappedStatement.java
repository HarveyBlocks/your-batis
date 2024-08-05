package org.harvey.batis.mapping;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.harvey.batis.enums.SqlCommandType;
import org.harvey.batis.exception.UnfinishedFunctionException;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-02 15:11
 */
public class MappedStatement {
    @Getter
    private String id;
    @Getter
    private SqlCommandType sqlCommandType;
    @Getter
    private String resource;
}
