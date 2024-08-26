package org.harvey.batis.builder.xml;

import org.harvey.batis.builder.BaseBuilder;
import org.harvey.batis.config.Configuration;
import org.harvey.batis.datasource.DataSourceFactory;
import org.harvey.batis.datasource.UnpooledDataSourceFactory;
import org.harvey.batis.exception.builder.BuilderException;
import org.harvey.batis.io.Resources;
import org.harvey.batis.mapping.Environment;
import org.harvey.batis.parsing.ConfigXmlConstants;
import org.harvey.batis.parsing.XNode;
import org.harvey.batis.parsing.XPathParser;
import org.harvey.batis.reflection.DefaultReflectorFactory;
import org.harvey.batis.reflection.ReflectorFactory;
import org.harvey.batis.transaction.TransactionFactory;
import org.harvey.batis.util.ErrorContext;
import org.harvey.batis.util.XPathBuilder;
import org.xml.sax.InputSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Properties;

/**
 * TODO
 * 解析YourBatis的config.xml文件的Builder<br>
 * 然后解析各Mapper.class-Mapper.xml, 然后组合<br>
 * 本类一个实例只能解析一次(因为流在解析一次之后回将其关闭)
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-04 17:28
 */
public class XMLConfigBuilder extends BaseBuilder {
    private static final XPathBuilder XPATH_BUILDER = new XPathBuilder()
            .setNamespace(ConfigXmlConstants.NAMESPACE_PREFIX).setAbsolute(false);

    private static String childXpath(String... elementName) {
        XPATH_BUILDER.clear();
        for (String name : elementName) {
            XPATH_BUILDER.findGradually(name);
        }
        return XPATH_BUILDER.toString();
    }

    /**
     * 是否已经解析过, 保证{@link #parse()}函数只被调用一遍
     */
    private boolean parsed;
    /**
     * @see XPathParser
     */
    private final XPathParser parser;
    /**
     * TODO
     * Datasource的环境, 但是暂未实现
     */
    private final String environment;
    private final ReflectorFactory localReflectorFactory = new DefaultReflectorFactory();

    /**
     * @see #XMLConfigBuilder(Reader, String, Properties)
     */
    public XMLConfigBuilder(Reader reader) {
        this(reader, null, null);
    }

    /**
     * @see #XMLConfigBuilder(Reader, String, Properties)
     */
    public XMLConfigBuilder(Reader reader, String environment) {
        this(reader, environment, null);
    }

    /**
     * @param reader 用于构建{@link InputSource}
     * @see #XMLConfigBuilder(InputSource, String, Properties)
     */
    public XMLConfigBuilder(Reader reader, String environment, Properties props) {
        this(new InputSource(reader), environment, props);
    }

    /**
     * @see #XMLConfigBuilder(InputStream, String, Properties)
     */
    public XMLConfigBuilder(InputStream inputStream) {
        this(inputStream, null, null);
    }

    /**
     * @see #XMLConfigBuilder(InputStream, String, Properties)
     */
    public XMLConfigBuilder(InputStream inputStream, String environment) {
        this(inputStream, environment, null);
    }

    /**
     * @param inputStream 用于构建{@link InputSource}
     * @see #XMLConfigBuilder(InputSource, String, Properties)
     */
    public XMLConfigBuilder(InputStream inputStream, String environment, Properties props) {
        this(new InputSource(inputStream), environment, props);
    }

    /**
     * @param source 用于构建{@link XPathParser}
     * @param props  存入{@link BaseBuilder#configuration}的{@link Configuration#setVariables(Properties)}里;<br>
     *               也会用来创建{@link XPathParser}, 此时, 就是当XML里有文本内容有调用存在, 就会用props中的值替换文本的key, <br>
     *               那么此时请参阅{@link org.harvey.batis.parsing.PropertyParser#parse(String, Properties)}
     * @see #XMLConfigBuilder(XPathParser, String, Properties)
     * @see org.harvey.batis.parsing.PropertyParser#parse(String, Properties)
     */
    public XMLConfigBuilder(InputSource source, String environment, Properties props) {
        this(new XPathParser(source, true, props,
                new XMLMapperEntityResolver(), ConfigXmlConstants.CONFIG_NAMESPACE_CONTEXT), environment, props);
    }

    /**
     * @param parser      {@link #parser}
     * @param environment {@link #environment}
     * @param props       用于构建{@link XPathParser}, 且存入父类字段{@link BaseBuilder#configuration}中
     */
    private XMLConfigBuilder(XPathParser parser, String environment, Properties props) {
        super(new Configuration());
        ErrorContext.instance().setResource("SQL Mapper Configuration");
        super.configuration.setVariables(props);
        this.parsed = false;
        this.environment = environment;
        this.parser = parser;
    }

    /**
     * @return {@link BaseBuilder#configuration}
     * @throws BuilderException 一个对象只能调用本函数一次, 多调用就抛出异常;<br>
     *                          解析失败也会抛出异常
     * @see #parseConfiguration(XNode)
     */
    public Configuration parse() {
        if (parsed) {
            // 只解析一遍
            throw new BuilderException("Each XMLConfigBuilder can only be used once.");
        }
        parsed = true;
        try {
            // 解析获取root节点
            String expression = XMLConfigBuilder.childXpath(ConfigXmlConstants.ROOT_ELEMENT);
            XNode root = parser.evaluateNode(expression);
            this.parseConfiguration(root);
        } catch (Exception e) {
            throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
        }
        return super.configuration;
    }


    /**
     * 解析config文件<br>
     * 解析Mapper文件, 将SQL语句于Mapper接口共同实现代理
     *
     * @param root 根节点
     * @see #environmentElement(XNode)
     * @see #mappersElement(XNode)
     * @see Configuration#addMappers(String)
     */
    private void parseConfiguration(XNode root) {
        try {
            this.propertiesElement(root);
            Environment env = this.environmentElement(root);
            configuration.setEnvironment(env);
            for (String mapperPackage : this.mappersElement(root)) {
                configuration.addMappers(mapperPackage);
            }
        } catch (Exception e) {
            throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
        }
    }

    private void propertiesElement(XNode root) {
        List<XNode> children = root.evaluateNode(childXpath(
                ConfigXmlConstants.PROPERTIES_ELEMENT
        )).getChildren();
        Properties defaults = new Properties();
        children.stream()
                .filter(node -> ConfigXmlConstants.RESOURCE_ELEMENT.equals(node.getName()))
                .forEach(node -> {
                    try {
                        defaults.putAll(Resources.getResourceAsProperties(
                                node.getAttributeValue(ConfigXmlConstants.FILEPATH_ATTRIBUTION)
                        ));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        children.stream().filter(node -> ConfigXmlConstants.PROPERTY_ELEMENT.equals(node.getName()))
                .forEach(node -> defaults.setProperty(
                        node.getAttributeValue(ConfigXmlConstants.KEY_ATTRIBUTION),
                        node.getAttributeValue(ConfigXmlConstants.VALUE_ATTRIBUTION)
                ));
        Properties vars = configuration.getVariables();
        if (vars != null) {
            defaults.putAll(vars);
        }
        parser.setVariables(defaults);
        configuration.setVariables(defaults);
    }

    /**
     * @return mapper.xml的路径, 在哪个路径下, 可以配置多个
     */
    private String[] mappersElement(XNode root) {
        List<XNode> children = root.evaluateNode(childXpath(
                ConfigXmlConstants.MAPPERS_ELEMENT
        )).getChildren();
        return children.stream()
                .map(child -> child.getAttributeValue(ConfigXmlConstants.MAPPERS_PATH_ATTRIBUTION))
                .toArray(String[]::new);
    }


    /**
     * 解析XML文件里的
     * {@link ConfigXmlConstants#TRANSACTION_MANAGER_ELEMENT},
     * {@link ConfigXmlConstants#DATABASE_ELEMENT},
     * {@link ConfigXmlConstants#DATABASE_ELEMENT},获取{@link XNode}其中的配置<br>
     * 然后实例化{@link TransactionFactory}和{@link DataSource}, 这俩封装进{@link Environment}
     */
    private Environment environmentElement(XNode child) throws Exception {
        if (child == null) {
            return null;
        }
        String transactionXpath = XMLConfigBuilder.childXpath(ConfigXmlConstants.TRANSACTION_MANAGER_ELEMENT);

        XNode transactionNode = child.evaluateNode(transactionXpath);
        TransactionFactory txFactory = this.transactionManagerElement(transactionNode);

        String databaseXpath = XMLConfigBuilder.childXpath(ConfigXmlConstants.DATABASE_ELEMENT);
        XNode databaseNode = child.evaluateNode(databaseXpath);
        String datasourceXpath = XMLConfigBuilder.childXpath(ConfigXmlConstants.DATASOURCE_ELEMENT);
        XNode datasourceNode = child.evaluateNode(datasourceXpath);

        DataSourceFactory dsFactory = this.dataSourceElement(databaseNode, datasourceNode);

        DataSource dataSource = dsFactory.getDataSource();

        org.harvey.batis.mapping.Environment.Builder environmentBuilder =
                new Environment.Builder()
                        .transactionFactory(txFactory)
                        .dataSource(dataSource);
        return environmentBuilder.build();

    }

    /**
     * 从transactionNode获取需要的TransactionFactory
     */
    private TransactionFactory transactionManagerElement(XNode transactionNode) throws Exception {
        if (transactionNode == null) {
            throw new BuilderException("Environment declaration requires a TransactionFactory.");
        }
        String type = transactionNode.getAttributeValue("type");
        return (TransactionFactory) resolveClass(type).getDeclaredConstructor().newInstance();
    }


    /**
     * 解析databaseNode和datasourceNode, 最终组成{@link DataSourceFactory}
     */
    private DataSourceFactory dataSourceElement(XNode databaseNode, XNode datasourceNode) throws Exception {
        if (databaseNode == null) {
            throw new BuilderException("Environment declaration requires a DataSourceFactory.");
        }
        String type = databaseNode.getAttributeValue("type");
        Properties props = this.getPropertiesFromDatabase(databaseNode);
        if (datasourceNode != null) {
            props = XMLConfigBuilder.union2Properties(datasourceNode.getChildrenAsProperties(), props);
        }
        String driverClass = databaseNode.getAttributeValue(ConfigXmlConstants.DRIVER_CLASS_ATTRIBUTION);
        props.setProperty(ConfigXmlConstants.DRIVER_CLASS_ATTRIBUTION, driverClass);
        DataSourceFactory factory = (DataSourceFactory) BaseBuilder.resolveClass(type).getDeclaredConstructor().newInstance();
        factory.setProperties(props);
        return factory;
    }

    /**
     * @param databaseNode 在XML文件中有字节点`auth`和`url`, 需要解析url, 然后吧auth的子节点存入Properties
     */
    private Properties getPropertiesFromDatabase(XNode databaseNode) {
        List<XNode> children = databaseNode.getChildren();
        Properties props = new Properties();
        for (XNode child : children) {
            switch (child.getName()) {
                case ConfigXmlConstants.DATABASE_URL_ELEMENT:
                    props.setProperty(ConfigXmlConstants.DATABASE_URL_ELEMENT,
                            UnpooledDataSourceFactory.parseUrl(child.getChildrenAsProperties(), child.getAttributes())
                    );
                    break;
                case ConfigXmlConstants.DATABASE_AUTH_ELEMENT:
                    props = XMLConfigBuilder.union2Properties(child.getChildrenAsProperties(), props);
                    break;
                default:
                    throw new BuilderException("Unknown xml node name: " + child.getName());
            }
        }
        return props;
    }

    /**
     * 将两个properties合并(不写入)
     *
     * @return 新的properties
     */
    private static Properties union2Properties(Properties prop1, Properties prop2) {
        Properties result = new Properties();
        prop1.forEach((k, v) -> result.setProperty(String.valueOf(k), String.valueOf(v)));
        prop2.forEach((k, v) -> result.setProperty(String.valueOf(k), String.valueOf(v)));
        return result;
    }


}
