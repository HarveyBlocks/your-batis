package org.harvey.batis.executor.loader;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-23 21:53
 */
public class ResultLoaderMap {
    private final Map<String, ?> loaderMap = new HashMap<>();

    public int size() {
        return loaderMap.size();
    }
}
