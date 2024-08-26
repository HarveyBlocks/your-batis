package org.harvey.batis.scripting.js;

import org.harvey.batis.exception.builder.BuilderException;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-25 18:08
 */
public class ExpressionEvaluator {
    public static boolean evaluateBoolean(
            String expression, Map<String, Object> parameterObject) {
        Object value = LanguagePhaser.phase(expression, parameterObject);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return new BigDecimal(String.valueOf(value)).compareTo(BigDecimal.ZERO) != 0;
        }
        return value != null;
    }

}
