package org.harvey.batis.builder;

import org.harvey.batis.exception.builder.IncompleteElementException;
import org.harvey.batis.mapping.ResultMap;
import org.harvey.batis.mapping.ResultMapping;

import java.util.List;

/**
 * ResultMap的解析器
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-04 16:03
 */
public class ResultMapResolver {

    private final MapperBuilderAssistant assistant;
    private final String id;
    private final Class<?> type;
    private final List<ResultMapping> resultMappings;

    public ResultMapResolver(MapperBuilderAssistant assistant, String id, Class<?> type, List<ResultMapping> resultMappings) {
        this.assistant = assistant;
        this.id = id;
        this.type = type;
        this.resultMappings = resultMappings;
    }

    /**
     * @see MapperBuilderAssistant#addResultMap(String, Class, List)
     */
    public ResultMap resolve() {
        return assistant.addResultMap(this.id, this.type, this.resultMappings);
    }
}
