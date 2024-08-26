package org.harvey.batis.builder.xml;

import org.harvey.batis.builder.MapperBuilderAssistant;
import org.harvey.batis.config.Configuration;
import org.harvey.batis.exception.UnfinishedFunctionException;
import org.w3c.dom.Node;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-11 17:29
 */
public class XMLIncludeTransformer {
    private final Configuration configuration;
    private final MapperBuilderAssistant builderAssistant;

    public XMLIncludeTransformer(Configuration configuration, MapperBuilderAssistant builderAssistant) {
        this.configuration = configuration;
        this.builderAssistant = builderAssistant;
    }

    public void applyIncludes(Node context) {
        throw new UnfinishedFunctionException(context);
    }
}
