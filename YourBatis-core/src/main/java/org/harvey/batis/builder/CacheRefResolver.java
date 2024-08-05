package org.harvey.batis.builder;

import lombok.AllArgsConstructor;
import org.harvey.batis.cache.Cache;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-04 16:03
 */
@AllArgsConstructor
public class CacheRefResolver {
    /**
     * TODO
     */
    private final MapperBuilderAssistant assistant;
    /**
     * TODO
     */
    private final String cacheRefNamespace;

    /**
     * TODO
     */
    public Cache resolveCacheRef() {
        return assistant.useCacheRef(cacheRefNamespace);
    }
}