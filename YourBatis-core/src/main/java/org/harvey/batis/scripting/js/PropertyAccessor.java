package org.harvey.batis.scripting.js;

import java.util.Map;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-25 21:13
 */
public interface PropertyAccessor {
    Object getProperty(Map<?, ?> context, Object target, Object name);

    void setProperty(Map<?, ?> context, Object target, Object name, Object value);

}
