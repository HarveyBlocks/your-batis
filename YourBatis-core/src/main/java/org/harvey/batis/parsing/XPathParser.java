package org.harvey.batis.parsing;

import lombok.Setter;
import org.harvey.batis.exception.builder.BuilderException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

/**
 * 包装了java原生的{@link XPath}, 用于解析XML文件
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-04 17:33
 */
public class XPathParser {

    /**
     * 一个原生java的XML的节点实例
     *
     * @see Document
     */
    private final Document document;
    /**
     * 在解析XML的时候是否需要验证(需要验证就会使用到{@link #entityResolver}和{@link #errorHandler})
     */
    private boolean validation;
    /**
     * 在解析XML文件时用到的解析器
     */
    private EntityResolver entityResolver;
    /**
     * 变量, 会被存入Xml类型的包装类
     * @see XNode#XNode(XPathParser, Node, Properties)
     */
    @Setter
    private Properties variables;
    /**
     * 每次实例化保证都有一个新的Xpath
     *
     * @see XPath
     */
    private XPath xPath;

    /**
     * 在解析XML文件时如果遇到异常的异常处理器
     */
    private final ErrorHandler errorHandler = new DefaultErrorHandler();


    /**
     * @param reader 用于转变为InputSource
     * @see #XPathParser(InputSource, boolean, Properties, EntityResolver)
     */
    public XPathParser(Reader reader, boolean validation, Properties variables, EntityResolver entityResolver) {
        this(new InputSource(reader), validation, variables, entityResolver);
    }

    /**
     * @param inputStream 用于转变为InputSource
     * @see #XPathParser(InputSource, boolean, Properties, EntityResolver)
     */
    public XPathParser(InputStream inputStream, boolean validation, Properties variables, EntityResolver entityResolver) {
        this(new InputSource(inputStream), validation, variables, entityResolver);
    }

    /**
     * @see #commonConstructor(boolean, Properties, EntityResolver)
     * @see #createDocument(InputSource)
     */
    public XPathParser(InputSource source, boolean validation, Properties variables, EntityResolver entityResolver) {
        this.commonConstructor(validation, variables, entityResolver);
        this.document = this.createDocument(source);
    }

    /**
     * @param document {@link #document}
     * @see #commonConstructor(boolean, Properties, EntityResolver)
     */
    public XPathParser(Document document) {
        this.commonConstructor(false, null, null);
        this.document = document;
    }


    /**
     * @param validation     {@link #validation}
     * @param variables      {@link #variables}
     * @param entityResolver {@link #entityResolver}
     */
    private void commonConstructor(boolean validation, Properties variables, EntityResolver entityResolver) {
        this.validation = validation;
        this.entityResolver = entityResolver;
        this.variables = variables;
        XPathFactory factory = XPathFactory.newInstance();
        this.xPath = factory.newXPath();
        this.xPath.setNamespaceContext(ConfigXmlConstants.CONFIG_NAMESPACE_CONTEXT);
    }

    /**
     * 必须在{@link #commonConstructor}之后调用<br>
     * 解析XML文件之后, 将文件内的实体({@link Document})存入字段{@link #document}
     *
     * @see #instanceBuilderFactory()
     * @see #instanceBuilder(DocumentBuilderFactory)
     */
    private Document createDocument(InputSource inputSource) {
        try {
            DocumentBuilderFactory factory = this.instanceBuilderFactory();
            DocumentBuilder builder = this.instanceBuilder(factory);
            return builder.parse(inputSource);
        } catch (Exception e) {
            throw new BuilderException("Error creating document instance.  Cause: " + e, e);
        }
    }


    /**
     * 作为XML的Schema
     */
    private static final String SCHEMA_LANGUAGE_ATTRIBUTE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    /**
     * 使用XSD作为Schema
     */
    private static final String XSD_SCHEMA_LANGUAGE = XMLConstants.W3C_XML_SCHEMA_NS_URI;

    /**
     * 创建出{@link DocumentBuilderFactory}, 并配置{@link #validation}等
     */
    private DocumentBuilderFactory instanceBuilderFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // 功能安全处理
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        // 是否需要验证
        factory.setValidating(validation);
        // 感知Namespace
        factory.setNamespaceAware(true);
        factory.setAttribute(SCHEMA_LANGUAGE_ATTRIBUTE, XSD_SCHEMA_LANGUAGE);
        // 忽略注释
        factory.setIgnoringComments(true);
        // 不忽略元素内容空格
        factory.setIgnoringElementContentWhitespace(false);
        // 不凝聚
        factory.setCoalescing(false);
        // 展开实体引用
        factory.setExpandEntityReferences(true);
        // 创建
        return factory;
    }

    /**
     * 创建出{@link DocumentBuilder}, 并配置{@link #entityResolver}和{@link #errorHandler}
     */
    private DocumentBuilder instanceBuilder(DocumentBuilderFactory factory) throws ParserConfigurationException {
        DocumentBuilder builder = factory.newDocumentBuilder();
        // XMLMapperEntityResolver
        builder.setEntityResolver(entityResolver);
        builder.setErrorHandler(errorHandler);
        return builder;
    }


    // ↑解析XML文件; ↓解析XML实体;

    /**
     * @see #evaluateNode(Object, String)
     */
    public XNode evaluateNode(String expression) {
        return this.evaluateNode(document, expression);
    }

    /**
     * @param root       解析源, 被解析后会转变为XNode类型
     * @param expression 被解析的{@link XPath}表达式
     * @see #evaluate(String, Object, QName)
     */
    public XNode evaluateNode(Object root, String expression) {
        Node node = (Node) this.evaluate(expression, root, XPathConstants.NODE);
        if (node == null) {
            return null;
        }
        return new XNode(this, node, variables);
    }
    /**
     * evaluate, 解析字符串表达式的意思
     *
     * @param returnType 决定返回的类型
     * @return 类型取决于 {@param returnType}
     */
    private Object evaluate(String expression, Object root, QName returnType) {
        try {
            return xPath.evaluate(expression, root, returnType);
        } catch (Exception e) {
            throw new BuilderException("Error evaluating XPath.  Cause: " + e, e);
        }
    }


}
