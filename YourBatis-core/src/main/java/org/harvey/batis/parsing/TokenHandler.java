package org.harvey.batis.parsing;

/**
 * {@link GenericTokenParser}提供content之后, 本类处理content, 然后交还给content给GenericTokenParser<br>
 * GenericTokenParser把原来的text和处理后content组合
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-05 16:18
 * @see GenericTokenParser
 */
@FunctionalInterface
public interface TokenHandler {
    /**
     * 处理content并返回
     */
    String handleToken(String content);
}

