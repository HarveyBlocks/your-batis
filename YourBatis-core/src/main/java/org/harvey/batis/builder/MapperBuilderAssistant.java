package org.harvey.batis.builder;

import org.harvey.batis.cache.Cache;
import org.harvey.batis.exception.UnfinishedFunctionException;
import org.harvey.batis.exception.builder.IncompleteElementException;
import org.harvey.batis.mapping.Discriminator;
import org.harvey.batis.mapping.ResultMap;
import org.harvey.batis.mapping.ResultMapping;

import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-04 16:04
 */
public class MapperBuilderAssistant {

    /**
     * TODO
     */
    public Cache useCacheRef(String namespace) {
        throw new UnfinishedFunctionException();
    }

    /**
     * TODO
     * @throws IncompleteElementException
     */
    public ResultMap addResultMap(String id, Class<?> type, String extend, Discriminator discriminator, List<ResultMapping> resultMappings, Boolean autoMapping) {
        throw new UnfinishedFunctionException();
    }
}
