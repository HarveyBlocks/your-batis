package org.harvey.batis.parsing;

import java.util.Properties;

/**
 * 将text文本中的被${}包围的key(content)替换成{@link PropertyParser.VariableTokenHandler#variables}里的值
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-05 16:17
 * @see #parse(String, Properties)
 */
public class PropertyParser {

    private static final String KEY_PREFIX = "org.harvey.batis.parsing.PropertyParser.";
    /**
     * 指示是否在占位符上启用默认值的特殊属性键。
     * <p>
     * 默认  {@link #ENABLE_DEFAULT_VALUE}. (指示禁用占位符的默认值)<br>
     * 如果指定未 {@code true}, 就可以在占位符上指定键和默认值(例如 {@code ${db.username:postgres}}).
     * </p>
     */
    public static final String KEY_ENABLE_DEFAULT_VALUE = KEY_PREFIX + "enable-default-value";

    /**
     * 特殊属性键，用于指定键的分隔符的默认值。
     * <p>
     * 默认的操作符是 {@link #DEFAULT_VALUE_SEPARATOR}.
     * </p>
     */
    public static final String KEY_DEFAULT_VALUE_SEPARATOR = KEY_PREFIX + "default-value-separator";

    private static final String ENABLE_DEFAULT_VALUE = "false";
    private static final String DEFAULT_VALUE_SEPARATOR = ":";

    public static final String VARIABLE_OPEN_TOKEN = "${";
    public static final String VARIABLE_CLOSE_TOKEN = "}";

    private PropertyParser() {
    }

    /**
     * 解析文本, 文本中含有部分形如"{@code ${key:default}}"的, key:default会被单独提出来<br>
     * 然后用key去往{@link VariableTokenHandler#variables}中取值, 取出值后嵌回text
     *
     * @param text      需要被解析的文本
     * @param variables {@link VariableTokenHandler#variables}
     * @return 解析(替换内容)后的文本
     * @see GenericTokenParser
     * @see GenericTokenParser#parse(String)
     * @see VariableTokenHandler
     * @see VariableTokenHandler#handleToken(String)
     */
    public static String parse(String text, Properties variables) {
        VariableTokenHandler handler = new VariableTokenHandler(variables);
        GenericTokenParser parser = new GenericTokenParser(VARIABLE_OPEN_TOKEN, VARIABLE_CLOSE_TOKEN, handler);
        return parser.parse(text);
    }

    /**
     * {@inheritDoc}
     * 解析content, 可以以"#{key:default}"的形式获从{@link #variables}取值
     *
     * @see #handleToken(String)
     */
    private static class VariableTokenHandler implements TokenHandler {
        /**
         * content将从variables中取值
         * <p>如果含有字段{@link #KEY_ENABLE_DEFAULT_VALUE}, 其值为true/false,<br>
         * 可以决定在解析({@link #handleToken(String)})content时是否解析默认值<br>
         * 如果不含有字段{@link #KEY_ENABLE_DEFAULT_VALUE}, 默认{@link #ENABLE_DEFAULT_VALUE}不开启<br></p>
         * <p>如果含有字段{@link #KEY_DEFAULT_VALUE_SEPARATOR}, 其value将被作为key-default的分隔符<br>
         * 如果不含有字段{@link #KEY_ENABLE_DEFAULT_VALUE}, 默认{@link #ENABLE_DEFAULT_VALUE}<br></p>
         *
         * @see #handleToken(String)
         */
        private final Properties variables;
        /**
         * 开启默认值
         */
        private final boolean enableDefaultValue;
        /**
         * 默认分隔符
         */
        private final String defaultValueSeparator;

        /**
         * @param variables {@link #variables}
         */
        private VariableTokenHandler(Properties variables) {
            this.variables = variables;
            this.enableDefaultValue = Boolean.parseBoolean(this.getPropertyValue(KEY_ENABLE_DEFAULT_VALUE, ENABLE_DEFAULT_VALUE));
            this.defaultValueSeparator = this.getPropertyValue(KEY_DEFAULT_VALUE_SEPARATOR, DEFAULT_VALUE_SEPARATOR);
        }

        /**
         * 从{@link #variables}中取值, 当{@link #variables}={@code null}, 则返回defaultValue
         *
         * @param defaultValue {@link #variables}为null或不含Key的时候返回的值
         */
        private String getPropertyValue(String key, String defaultValue) {
            return (variables == null) ? defaultValue : variables.getProperty(key, defaultValue);
        }

        /**
         * <li>从{@link #variables}中, 依据content获取值(如果取得到)
         * <pre>{@code
         *                return variables.getProperty(key);
         *      }</pre></li>
         * <li>如果{@link #variables}为null, 返回还原content:<br>
         * <pre>{@code
         *               return VARIABLE_OPEN_TOKEN + content+ VARIABLE_CLOSE_TOKEN;
         *      }</pre></li>
         * <li>如果{@link #variables}不为null, 用content作为key取不到值<br>
         * <pre>{@code
         *               return VARIABLE_OPEN_TOKEN + content+ VARIABLE_CLOSE_TOKEN;
         *      }</pre></li>
         * <li>如果 {@link #enableDefaultValue}={@code true}, <br>
         * 而{@param content} 中含有形如"key:default"的(其中":", 取决于{@link #defaultValueSeparator}):<br>
         * 从content解析出defaultValue和key从{@link #variables}取值, 然后
         * <pre>{@code
         *              return variables.getProperty(key, defaultValue);
         *      }</pre>(不会加前后缀了)</li>
         * <ul>
         *      <li>若content="content:", 则key="content", defaultValue=""</li>
         * </ul>
         *
         * @return 返回从variables中取得的值之后, 替换到原text的位置
         */
        @Override
        public String handleToken(String content) {
            if (variables == null) {
                return VARIABLE_OPEN_TOKEN + content + VARIABLE_CLOSE_TOKEN;
            }
            String key = content;
            if (enableDefaultValue) {
                final int separatorIndex = content.indexOf(defaultValueSeparator);
                String defaultValue = null;
                if (separatorIndex >= 0) {
                    key = content.substring(0, separatorIndex);
                    // 获取content中`:`(defaultValueSeparator)分隔符后面的内容
                    defaultValue = content.substring(separatorIndex + defaultValueSeparator.length());
                }
                if (defaultValue != null) {
                    return variables.getProperty(key, defaultValue);
                }
            }
            if (variables.containsKey(key)) {
                return variables.getProperty(key);
            }
            return VARIABLE_OPEN_TOKEN + content + VARIABLE_CLOSE_TOKEN;
        }
    }

}
