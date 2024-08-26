package org.harvey.batis.scripting.xml.node;

import lombok.Getter;
import org.harvey.batis.builder.SqlSourceBuilder;
import org.harvey.batis.exception.scripting.ScriptingException;
import org.harvey.batis.parsing.GenericTokenParser;
import org.harvey.batis.parsing.TokenHandler;
import org.harvey.batis.scripting.js.LanguagePhaser;
import org.harvey.batis.scripting.xml.DynamicContext;
import org.harvey.batis.scripting.xml.SqlNode;
import org.harvey.batis.util.type.SimpleTypeRegistry;

import java.util.Properties;
import java.util.regex.Pattern;

/**
 * 区别与{@link StaticTextSqlNode}, <br>
 * 当文本含有形如{@code ${}}的(内容不存在映射)存在, 则为本类, <br>
 * 否则视作{@link StaticTextSqlNode}<br>
 * {@code ${}} 的内容将被作为解释器解析
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-12 00:07
 */
public class TextSqlNode implements SqlNode {
    private final String text;
    /**
     * 用于检查被解析后的字符串部分是否符合某种标准
     */
    private final Pattern injectionFilter;
    public static final String SCRIPT_OPEN_TOKEN = SqlSourceBuilder.SCRIPT_OPEN_TOKEN;
    public static final String SCRIPT_CLOSE_TOKEN = SqlSourceBuilder.SCRIPT_CLOSE_TOKEN;

    public TextSqlNode(String text) {
        this(text, null);
    }

    public TextSqlNode(String text, Pattern injectionFilter) {
        this.text = text;
        this.injectionFilter = injectionFilter;
    }

    /**
     * 由于XNode的Body, 实现进行过{@link org.harvey.batis.parsing.PropertyParser#parse(String, Properties)}<br>
     * 所以, 要想Dynamic为true, 也就是说此时的body仍然含有{@code ${}}, <br>
     * 那么就需要{@code ${}}里的内容不会匹配到properties
     *
     * @see DynamicCheckerTokenParser#isDynamic()
     */
    public boolean isDynamic() {
        DynamicCheckerTokenParser checker = new DynamicCheckerTokenParser();
        GenericTokenParser parser = createParser(checker);
        parser.parse(text);
        return checker.isDynamic();

    }

    @Getter
    private static class DynamicCheckerTokenParser implements TokenHandler {
        /**
         * 凡是含有能被{@link GenericTokenParser}获取到的<br>
         * 视作Dynamic, 此为true
         */
        private boolean isDynamic;

        public DynamicCheckerTokenParser() {
            // Prevent Synthetic Access
        }

        @Override
        public String handleToken(String content) {
            this.isDynamic = true;
            return null;
        }
    }

    private GenericTokenParser createParser(TokenHandler handler) {
        return new GenericTokenParser(SCRIPT_OPEN_TOKEN, SCRIPT_CLOSE_TOKEN, handler);
    }

    @Override
    public boolean apply(DynamicContext context) {
        GenericTokenParser parser = this.createParser(
                new BindingTokenParser(context, injectionFilter));
        context.appendSql(parser.parse(text));
        return true;
    }

    private static class BindingTokenParser implements TokenHandler {
        private final DynamicContext context;
        /**
         * @see TextSqlNode#injectionFilter
         */
        private final Pattern injectionFilter;

        public BindingTokenParser(DynamicContext context, Pattern injectionFilter) {
            this.context = context;
            this.injectionFilter = injectionFilter;
        }

        @Override
        public String handleToken(String content) {
            Object parameter = context.getBindings().get("_parameter");
            if (parameter == null) {
                context.getBindings().put("value", null);
            } else if (SimpleTypeRegistry.isSimpleType(parameter.getClass())) {
                // 如果是简单类型, 在SQL中能够用value来获取parameter
                context.getBindings().put("value", parameter);
            }
            Object value = LanguagePhaser.phase(content, context.getBindings());
            String srtValue = value == null ? "" : value.toString();
            checkInjection(srtValue);
            return srtValue;
        }

        /**
         * 结果是否符合{@link #injectionFilter}
         *
         * @param value 待检查的最终结果字符串
         */
        private void checkInjection(String value) {
            if (injectionFilter != null && !injectionFilter.matcher(value).matches()) {
                throw new ScriptingException("Invalid input. Please conform to regex" + injectionFilter.pattern());
            }
        }
    }
}
