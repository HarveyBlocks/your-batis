package org.harvey.batis.mapping;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-21 01:37
 */
@Getter
public class ParameterMap {
    private String id;
    private Class<?> type;
    private List<ParameterMapping> parameterMappings;

    private ParameterMap() {
    }

    public static class Builder {
        private final ParameterMap product = new ParameterMap();

        public Builder(String id, Class<?> type, List<ParameterMapping> parameterMappings) {
            product.id = id;
            product.type = type;
            product.parameterMappings = parameterMappings;
        }

        public Class<?> type() {
            return product.type;
        }

        public ParameterMap build() {
            //lock down collections
            product.parameterMappings = Collections.unmodifiableList(product.parameterMappings);
            return product;
        }
    }
}
