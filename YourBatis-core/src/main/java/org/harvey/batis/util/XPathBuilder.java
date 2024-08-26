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
@Getter
@Accessors(chain = true)
public class XPathBuilder {
    public static final String ABSOLUTE_PATH = "/";
    public static final String ALL_LAYER_PATH = "//";
    public static final String NAMESPACE_SEPARATOR = ":";
    public static final String NOW_POSITION = ".";
    public static final String PRE_POSITION = "..";
    public static final String ANY = "*";
    public static final String UNION = "|";
    public static final String ATTRIBUTION_PREFIX = "@";
    public static final Function<String, String> AT_WRAPPER = (str) -> "[" + str + "]";

    private final StringBuilder builder;
    /**
     * 设置一次, 之后所有的find都有效~
     */
    @Setter
    private String namespace = null;
    /**
     * 开启绝对路径
     */
    @Setter
    private boolean absolute = true;

    public XPathBuilder() {
        this(new StringBuilder());
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

    private XPathBuilder append(String str) {
        this.builder.append(str);
        return this;
    }

    public void clear() {
        this.builder.setLength(0);
    }

    @Override
    public String toString() {
        String result = this.builder.toString();
        if (!absolute && result.startsWith(ABSOLUTE_PATH)) {
            // 想要相对路径
            return result.substring(1, builder.length());
        }
        return result;
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
        return this.append(gradually ? ABSOLUTE_PATH : ALL_LAYER_PATH)
                .appendElementPathWithNs(elementName);
    }

    private XPathBuilder appendElementPathWithNs(String elementName) {
        if (!NOW_POSITION.equals(elementName) && !PRE_POSITION.equals(elementName)) {
            // 不是`.`,不是`..`, 可以加ns
            if (this.namespace != null && !this.namespace.isEmpty()) {
                this.append(this.namespace).append(NAMESPACE_SEPARATOR);
            }
        }
        return this.append(elementName);
    }

    /**
     * "//cfg:database"
     * "/cfg:config//cfg:username"
     */
    public XPathBuilder findAllLayer(String elementName) {
        return this.find(elementName, false);
    }

    public XPathBuilder attribution(String attribute) {
        return this.append(ATTRIBUTION_PREFIX).append(attribute);
    }

    public XPathBuilder anyAttribution() {
        return this.attribution(ANY);
    }

    public XPathBuilder anyElement() {
        return this.findGradually(ANY);
    }

    public XPathBuilder union(String elementName) {
        return this.union().appendElementPathWithNs(elementName);
    }

    public XPathBuilder union() {
        return this.append(UNION);
    }

    public XPathBuilder at(String expression) {
        if (expression == null || expression.isEmpty()) {
            return this;
        }
        return this.append(AT_WRAPPER.apply(expression));
    }

    public XPathBuilder atAnyAttribution() {
        return this.at(ATTRIBUTION_PREFIX + ANY);
    }


}
