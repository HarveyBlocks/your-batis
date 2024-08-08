package org.harvey.batis.util;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.Function;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-06 14:56
 */
@Accessors(chain = true)
public class XPathBuilder {
    public static final String GRADUAL_FIND = "/";
    public static final String ALL_LAYER_FIND = "//";
    public static final String NAMESPACE_SEPARATOR = ":";
    public static final String NOW_POSITION = ".";
    public static final String PRE_POSITION = "..";
    public static final String ANY = "*";
    public static final String UNION = "|";
    public static final String ATTRIBUTION_PREFIX = "@";
    public static final Function<String, String> AT_WRAPPER = (str) -> "[" + str + "]";

    @Getter
    private final StringBuilder builder;
    /**
     * 设置一次, 之后所有的find都有效~
     */
    @Setter
    private String namespace = null;


    public XPathBuilder() {
        builder = new StringBuilder();
    }

    public XPathBuilder(StringBuilder builder) {
        this.builder = builder;
    }

    public XPathBuilder atNow() {
        return this.findGradually(NOW_POSITION);
    }

    public XPathBuilder returnPre() {
        return this.findGradually(PRE_POSITION);
    }

    /**
     * 构造/config/student/name
     */
    public XPathBuilder findGradually(String elementName) {
        return this.find(elementName, true);
    }

    public XPathBuilder find(String elementName, boolean gradually) {
        if (elementName == null || elementName.isEmpty()) {
            return this;
        }
        builder.append(gradually ? GRADUAL_FIND : ALL_LAYER_FIND);
        if (!NOW_POSITION.equals(elementName) && !PRE_POSITION.equals(elementName)) {
            // 不是`.`,不是`..`, 可以加ns
            if (this.namespace != null && !this.namespace.isEmpty()) {
                builder.append(this.namespace).append(NAMESPACE_SEPARATOR);
            }
        }
        builder.append(elementName);
        return this;
    }

    /**
     * "//cfg:database"
     * "/cfg:config//cfg:username"
     */
    public XPathBuilder findAllLayer(String elementName) {
        return this.find(elementName, false);
    }

    public XPathBuilder attribution(String attribute) {
        builder.append(ATTRIBUTION_PREFIX).append(attribute);
        return this;
    }

    public XPathBuilder anyAttribution() {
        return this.attribution(ANY);
    }

    public XPathBuilder anyElement() {
        return this.findGradually(ANY);
    }

    public XPathBuilder union() {
        builder.append(UNION);
        return this;
    }

    public XPathBuilder at(String expression) {
        if (expression == null || expression.isEmpty()) {
            return this;
        }
        builder.append(AT_WRAPPER.apply(expression));
        return this;
    }

    public XPathBuilder atAnyAttribution() {
        return this.at(ATTRIBUTION_PREFIX + ANY);
    }

    public String clear() {
        String result = this.builder.toString();
        this.builder.setLength(0);
        return result;
    }

    @Override
    public String toString() {
        return this.builder.toString();
    }
}
