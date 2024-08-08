package org.harvey.batis.io;

import java.io.IOException;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-08 13:52
 */
public interface ResourceAccessor {
    /**
     * @return {@link ResourceAccessor} 实现对当前环境有效, 则返回true
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isValid();

    /**
     * 递归列出所有资源的完整资源路径，这些资源包括指定路径中所有子资源。
     *
     * @param path 要列出的资源的路径。
     * @return 包含子资源名称的List。
     */
    List<String> list(String path) throws IOException;
}
