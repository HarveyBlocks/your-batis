package org.harvey.batis.mapping;

import lombok.Getter;
import org.harvey.batis.config.Configuration;
import org.harvey.batis.util.enums.JdbcType;
import org.harvey.batis.util.enums.ParameterMode;
import org.harvey.batis.util.type.TypeHandler;
import org.harvey.batis.util.type.TypeHandlerRegistry;

/**
 * TODO
 * 针对#{}的mapping
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-13 17:08
 */
@Getter
public class ParameterMapping {
    private Configuration configuration;
    private String property;
    private ParameterMode mode;
    private Class<?> javaType = Object.class;
    private JdbcType jdbcType;
    private TypeHandler<?> typeHandler;
    private String resultMapId;

    public static class Builder {
        private final ParameterMapping product = new ParameterMapping();

        public Builder(Configuration configuration, String property, TypeHandler<?> typeHandler) {
            product.configuration = configuration;
            product.property = property;
            product.typeHandler = typeHandler;
            product.mode = ParameterMode.IN;
        }

        public Builder(Configuration configuration, String property, Class<?> javaType) {
            product.configuration = configuration;
            product.property = property;
            product.javaType = javaType;
            product.mode = ParameterMode.IN;
        }

        public Builder typeHandler(TypeHandler<?> typeHandler) {
            product.typeHandler = typeHandler;
            return this;
        }

        public Builder javaType(Class<?> javaType) {
            product.javaType = javaType;
            return this;
        }

        public Builder mode(ParameterMode mode) {
            product.mode = mode;
            return this;
        }

        public Builder resultMapId(String resultMapId) {
            product.resultMapId = resultMapId;
            return this;
        }


        public ParameterMapping build() {
            resolveTypeHandler();
            validate();
            return product;
        }

        private void resolveTypeHandler() {
            if (product.typeHandler == null && product.javaType != null) {
                Configuration configuration = product.configuration;
                TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
                product.typeHandler = typeHandlerRegistry.getTypeHandler(product.javaType, product.jdbcType);
            }
        }

        private void validate() {
            if (product.typeHandler == null) {
                throw new IllegalStateException("Type handler was null on parameter mapping for property '"
                        + product.property + "'. It was either not specified and/or could not be found for the javaType ("
                        + product.javaType.getName() + ") : jdbcType (" + product.jdbcType + ") combination.");
            }
        }

    }
}
