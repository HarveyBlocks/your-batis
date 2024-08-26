package org.harvey.batis.executor;

import lombok.Getter;
import lombok.Setter;
import org.harvey.batis.mapping.MappedStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-14 17:00
 */
@Getter
public class BatchResult {
    private final MappedStatement mappedStatement;

    private final String sql;
    private final List<Object> parameterObjects;
    @Setter
    private int[] updateCounts;

    public BatchResult(MappedStatement mappedStatement, String sql) {
        super();
        this.mappedStatement = mappedStatement;
        this.sql = sql;
        this.parameterObjects = new ArrayList<>();
    }

    public BatchResult(MappedStatement mappedStatement, String sql, Object parameterObject) {
        this(mappedStatement, sql);
        addParameterObject(parameterObject);
    }

    public void addParameterObject(Object parameterObject) {
        this.parameterObjects.add(parameterObject);
    }

}
