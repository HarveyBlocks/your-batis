package org.harvey.batis.builder;

import org.harvey.batis.config.Configuration;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-04 16:10
 */
public abstract class BaseBuilder {
    protected final Configuration configuration;
    /*TODO
    protected final TypeAliasRegistry typeAliasRegistry;
    protected final TypeHandlerRegistry typeHandlerRegistry;*/

    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
        /*TODO
        this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
        this.typeHandlerRegistry = this.configuration.getTypeHandlerRegistry();*/
    }
}
