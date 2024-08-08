package org.harvey.batis.parsing;

import junit.framework.TestCase;
import org.harvey.batis.builder.xml.XMLMapperEntityResolver;
import org.harvey.batis.io.Resources;
import org.harvey.batis.util.XPathBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Iterator;

public class XPathParserTest extends TestCase {

    public void testEvaluateNode() {
        Reader reader;
        try {
            reader = Resources.getResourceAsReader("test.xml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        XPathParser xPathParser = new XPathParser(reader, true, null, new XMLMapperEntityResolver());
        String expression = new XPathBuilder()
                .setNamespace(ConfigXmlConstants.NAMESPACE_PREFIX)
                .findGradually(ConfigXmlConstants.ROOT_ELEMENT)
                .toString();
        System.out.println("expression = " + expression);

        XNode xNode = xPathParser.evaluateNode(expression);
        System.out.println(xNode);
    }

    public void testTestEvaluateNode() {
    }

    public void testSetVariables() {
    }

    private static final String SCHEMA_LANGUAGE_ATTRIBUTE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";


    public void testXPath() {
        testXPath0(true);
        testXPath0(false);
    }

    private static void testXPath0(boolean validating) {
        String filename = "test.xml";
        InputStream resource;
        try {
            resource = Resources.getResourceAsStream(filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // 功能安全处理
        /*try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }*/
        // 是否需要验证
        factory.setValidating(validating);
        // 感知Namespace
        factory.setNamespaceAware(true);
        factory.setAttribute(SCHEMA_LANGUAGE_ATTRIBUTE, XMLConstants.W3C_XML_SCHEMA_NS_URI);

        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

        builder.setEntityResolver(new XMLMapperEntityResolver());
        builder.setErrorHandler(new DefaultErrorHandler());

        Document document;
        try {
            document = builder.parse(resource);
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }

        XPath xPath = XPathFactory.newInstance().newXPath();
        String expression = "/cfg:config"; // "/*[local-name()='config']"
        NamespaceContext nsContext = new NamespaceContext() {
            public static final String PREFIX = "cfg";
            public static final String CONFIG = "http://batis.harvey.org/schema/config";
            public static final String XSI = "https://batis.harvey.org/schema/config";

            @Override
            public String getNamespaceURI(String prefix) {
                if (PREFIX.equals(prefix)) {
                    return CONFIG;
                }
                return XMLConstants.NULL_NS_URI;
            }

            @Override
            public String getPrefix(String namespaceURI) {
                return null;
            }

            @Override
            public Iterator<String> getPrefixes(String namespaceURI) {
                return null;
            }
        };
        xPath.setNamespaceContext(nsContext);
        try {
            Node node = (Node) xPath.evaluate(expression, document, XPathConstants.NODE);
            System.out.println(node);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }
}