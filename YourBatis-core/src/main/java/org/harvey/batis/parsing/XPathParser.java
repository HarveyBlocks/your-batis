package org.harvey.batis.parsing;

import lombok.Setter;
import org.harvey.batis.exception.builder.BuilderException;
import org.w3c.dom.Document;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-04 17:33
 */
public class XPathParser {

    private final Document document;
    /**
     * 验证
     */
    private boolean validation;
    /**
     * 实体解析程序
     */
    private EntityResolver entityResolver;
    /**
     * 变量
     */
    @Setter
    private Properties variables;
    private XPath xpath;
    private final ErrorHandler errorHandler = new ErrorHandler() {
        @Override
        public void error(SAXParseException exception) throws SAXException {
            throw exception;
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            throw exception;
        }

        @Override
        public void warning(SAXParseException exception) throws SAXException {
            // NOP
        }
    };


    public XPathParser(Reader reader, boolean validation, Properties variables, EntityResolver entityResolver) {
        this.commonConstructor(validation, variables, entityResolver);
        this.document = this.createDocument(new InputSource(reader));
    }

    public XPathParser(InputStream inputStream, boolean validation, Properties variables, EntityResolver entityResolver) {
        this.commonConstructor(validation, variables, entityResolver);
        this.document = this.createDocument(new InputSource(inputStream));
    }

    public XPathParser(Document document) {
        commonConstructor(false, null, null);
        this.document = document;
    }

    private void commonConstructor(boolean validation, Properties variables, EntityResolver entityResolver) {
        this.validation = validation;
        this.entityResolver = entityResolver;
        this.variables = variables;
        XPathFactory factory = XPathFactory.newInstance();
        this.xpath = factory.newXPath();
    }

    /**
     * 必须在{@link #commonConstructor}之后调用
     */
    private Document createDocument(InputSource inputSource) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        // Enforce namespace aware for XSD...
        factory.setNamespaceAware(true);
        try {
            try {
                factory.setAttribute(SCHEMA_LANGUAGE_ATTRIBUTE, XSD_SCHEMA_LANGUAGE);
            } catch (IllegalArgumentException ex) {
                ParserConfigurationException pcex = new ParserConfigurationException(
                        "Unable to validate using XSD: Your JAXP provider [" + factory +
                                "] does not support XML Schema. Are you running on Java 1.4 with Apache Crimson? " +
                                "Upgrade to Apache Xerces (or Java 1.5) for full XSD support.");
                pcex.initCause(ex);
                throw pcex;
            }
            DocumentBuilder builder = factory.newDocumentBuilder();
            if (entityResolver != null) {
                builder.setEntityResolver(entityResolver);
            }
            builder.setErrorHandler(errorHandler);
            return builder.parse(inputSource);
        } catch (Exception e) {
            throw new BuilderException("Error creating document instance.  Cause: " + e, e);
        }
    }

//    private Document createDocument(InputSource inputSource) {
//        try {
//            DocumentBuilderFactory factory = this.instanceBuilderFactory();
//            DocumentBuilder builder = instanceBuilder(factory);
//            return builder.parse(inputSource);
//        } catch (Exception e) {
//            throw new BuilderException("Error creating document instance.  Cause: " + e, e);
//        }
//    }


    /**
     * JAXP attribute used to configure the schema language for validation.
     */
    private static final String SCHEMA_LANGUAGE_ATTRIBUTE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    /**
     * JAXP attribute value indicating the XSD schema language.
     */
    private static final String XSD_SCHEMA_LANGUAGE = "http://www.w3.org/2001/XMLSchema";

    /**
     * TODO
     */
    private DocumentBuilderFactory instanceBuilderFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // 功能安全处理
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        // 是否需要验证
        factory.setValidating(validation);
        // 感知Namespace
        factory.setNamespaceAware(true);
        try {
            factory.setAttribute(SCHEMA_LANGUAGE_ATTRIBUTE, XSD_SCHEMA_LANGUAGE);
        } catch (IllegalArgumentException ex) {
            ParserConfigurationException pcex = new ParserConfigurationException(
                    "Unable to validate using XSD: Your JAXP provider [" + factory +
                            "] does not support XML Schema. Are you running on Java 1.4 with Apache Crimson? " +
                            "Upgrade to Apache Xerces (or Java 1.5) for full XSD support.");
            pcex.initCause(ex);
            throw pcex;
        }
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
     * TODO
     */
    private DocumentBuilder instanceBuilder(DocumentBuilderFactory factory) throws ParserConfigurationException {
        DocumentBuilder builder = factory.newDocumentBuilder();
        // XMLMapperEntityResolver
        builder.setEntityResolver(entityResolver);
        builder.setErrorHandler(errorHandler);
        return builder;
    }
}
