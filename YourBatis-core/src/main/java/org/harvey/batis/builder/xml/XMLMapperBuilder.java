package org.harvey.batis.builder.xml;

import org.harvey.batis.builder.BaseBuilder;
import org.harvey.batis.builder.MapperBuilderAssistant;
import org.harvey.batis.builder.ResultMapResolver;
import org.harvey.batis.config.Configuration;
import org.harvey.batis.exception.UnfinishedFunctionException;
import org.harvey.batis.exception.builder.BuilderException;
import org.harvey.batis.exception.builder.IncompleteElementException;
import org.harvey.batis.io.Resources;
import org.harvey.batis.mapping.ResultMap;
import org.harvey.batis.mapping.ResultMapping;
import org.harvey.batis.parsing.MapperXmlConstants;
import org.harvey.batis.parsing.XNode;
import org.harvey.batis.parsing.XPathParser;
import org.harvey.batis.util.ErrorContext;
import org.harvey.batis.util.XPathBuilder;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-09 15:55
 */
public class XMLMapperBuilder extends BaseBuilder {

    private static final XPathBuilder XPATH_BUILDER = new XPathBuilder().setNamespace(MapperXmlConstants.NAMESPACE_PREFIX).setAbsolute(false);

    private static String childXpath(String... elementName) {
        XPATH_BUILDER.clear();
        for (String name : elementName) {
            XPATH_BUILDER.findGradually(name);
        }
        return XPATH_BUILDER.toString();
    }

    private static String unionXpath(String... elementName) {
        XPATH_BUILDER.clear();
        if (elementName.length == 0) {
            return XPATH_BUILDER.toString();
        }
        XPATH_BUILDER.findGradually(elementName[0]);
        for (int i = 1; i < elementName.length; i++) {
            XPATH_BUILDER.union(elementName[i]);
        }
        return XPATH_BUILDER.toString();
    }

    private final XPathParser parser;
    private final MapperBuilderAssistant builderAssistant;
    private final Map<String, XNode> sqlFragments;
    private final String xmlResource;

    /**
     * @param mapperXmlIs
     * @param configuration
     * @param xmlResource
     * @param sqlFragments
     * @param targetMapper  希望同级目录下的同名XML文件的target属性值和这个参数一致
     */
    public XMLMapperBuilder(InputStream mapperXmlIs, Configuration configuration, String xmlResource, Map<String, XNode> sqlFragments, String targetMapper) {
        this(mapperXmlIs, configuration, xmlResource, sqlFragments);
        this.builderAssistant.setCurrentTargetMapper(targetMapper);
    }

    public XMLMapperBuilder(InputStream mapperXmlIs, Configuration configuration, String xmlResource, Map<String, XNode> sqlFragments) {
        this(new XPathParser(mapperXmlIs, true, configuration.getVariables(), new XMLMapperEntityResolver(), MapperXmlConstants.MAPPER_NAMESPACE_CONTEXT), configuration, xmlResource, sqlFragments);
    }

    private XMLMapperBuilder(XPathParser parser, Configuration configuration, String xmlResource, Map<String, XNode> sqlFragments) {
        super(configuration);
        this.builderAssistant = new MapperBuilderAssistant(configuration, xmlResource);
        this.parser = parser;
        this.sqlFragments = sqlFragments;
        this.xmlResource = xmlResource;
    }

    public void parse() {
        if (!configuration.isResourceLoaded(xmlResource)) {
            this.configurationElement(parser.evaluateNode(childXpath(MapperXmlConstants.ROOT_ELEMENT)));
            configuration.addLoadedResource(xmlResource);
            bindMapperForTarget();
        }
        configuration.parsePendingRemoveFinished(configuration.getIncompleteResultMaps(),
                ResultMapResolver::resolve, false);
        // 无Cache需求 parsePendingCacheRefs();
        configuration.parsePendingRemoveFinished(configuration.getIncompleteStatements(),
                XMLStatementBuilder::parseStatementNode, false);
    }

    private void configurationElement(XNode context) {
        try {
            String target = context.getAttributeValue(MapperXmlConstants.MAPPER_TARGET_ATTRIBUTE);
            if (target == null || target.isEmpty()) {
                throw new BuilderException("Mapper's namespace cannot be empty");
            }
            builderAssistant.setCurrentTargetMapper(target);
            UnfinishedFunctionException.trace("解析Mapper.xml的cache的Element");
            /*
            无需求
            cacheRefElement(context.evaluateNode("cache-ref"));
            cacheElement(context.evaluateNode("cache"));*/
            this.resultMapElements(context.evaluateNodes(childXpath(MapperXmlConstants.ResultMap.ELEMENT_NAME)));
            this.sqlElement(context.evaluateNodes(unionXpath(MapperXmlConstants.Sql.SQL_SELECT_ELEMENT, MapperXmlConstants.Sql.SQL_DELETE_ELEMENT, MapperXmlConstants.Sql.SQL_INSERT_ELEMENT, MapperXmlConstants.Sql.SQL_UPDATE_ELEMENT)));
            // this.this.buildStatementFromContext(context.evaluateNodes("select|insert|update|delete"));
        } catch (Exception e) {
            throw new BuilderException("Error parsing Mapper XML. The XML location is '" + xmlResource + "'. Cause: " + e, e);
        }
    }

    private void sqlElement(List<XNode> xNodes) {
        for (XNode context : xNodes) {
            // 解析每一个SQL的node
            final XMLStatementBuilder statementParser = new XMLStatementBuilder(configuration, builderAssistant, context, null);
            try {
                statementParser.parseStatementNode();
            } catch (IncompleteElementException e) {
                configuration.addIncompleteStatement(statementParser);
            }
        }
    }

    /**
     * 解析所有{@code <result-map/>} 标签
     *
     * @param xNodes {@code  <result-map/>} 标签组成的列表
     */
    private void resultMapElements(List<XNode> xNodes) {
        for (XNode resultMapNode : xNodes) {
            try {
                resultMapElement(resultMapNode);
            } catch (IncompleteElementException e) {
                // ignore, 一会儿重试
            }
        }
    }

    /**
     * 解析单个{@code <result/>} 标签
     *
     * @param resultMapNode {@code <result/>} 标签
     * @return 解析之后的 {@link ResultMap}
     */
    private ResultMap resultMapElement(XNode resultMapNode) {
        // 获取包含一切父级标签信息的ID
        String valueBasedId = resultMapNode.getValueBasedIdentifier(MapperXmlConstants.ResultMap.ID_ATTRIBUTION);
        ErrorContext.instance().setActivity("processing " + valueBasedId);
        // 获取Java的Entity类型的类型
        String type = resultMapNode.getAttributeValue(MapperXmlConstants.ResultMap.ENTITY_TYPE_ATTRIBUTION);
        Class<?> typeClass = BaseBuilder.resolveClass(type);
        // 接下来解析<result/>
        List<ResultMapping> resultMappings = new ArrayList<>();
        List<XNode> resultChildren = resultMapNode.getChildren();
        for (XNode resultChild : resultChildren) {
            // 解析每一个<result/>
            resultMappings.add(buildResultMappingFromContext(resultChild, typeClass));
        }
        // 简单获取ID(如果不能获取, 就获取valueBasedId)
        String id = resultMapNode.getAttributeValue(MapperXmlConstants.ResultMap.ID_ATTRIBUTION, valueBasedId);
        // 准备好所有resultMap的
        // 依据准备好的resultMap的各参数, 创造解析器ResultMapResolver
        ResultMapResolver resultMapResolver = new ResultMapResolver(builderAssistant, id, typeClass, resultMappings);
        try {
            // 解析器进行解析, 创造出ResultMap
            return resultMapResolver.resolve();
        } catch (IncompleteElementException e) {
            // 出现异常, 将解析器放到configuration里的IncompleteResultMap里,
            // 之后再进行解析的重试
            configuration.addIncompleteResultMap(resultMapResolver);
            throw e;
        }
    }

    /**
     * 准备字段并传入{@link MapperBuilderAssistant#buildResultMapping(Class, String, String, Class)}
     *
     * @param context     {@code <result/>}标签
     * @param entityClass ResultMap对应的实体类型;
     */
    private ResultMapping buildResultMappingFromContext(XNode context, Class<?> entityClass) {
        String field = context.getAttributeValue(MapperXmlConstants.ResultMap.MAP_ELEMENT_FIELD);
        String column = context.getAttributeValue(MapperXmlConstants.ResultMap.MAP_ELEMENT_COLUMN);
        /*
        TODO, 我的想法是将TypeHandler和JDBCType和JavaType单独出来作为一个Element
        但是现在还不是时候
        String typeHandler = context.getAttributeValue(MapperXmlConstants.MAP_ELEMENT_TYPE_HANDLER);
        Class<? extends TypeHandler<?>> typeHandlerClass = BaseBuilder.resolveClass(typeHandler);*/
        return builderAssistant.buildResultMapping(entityClass, field, column, null);
    }


    private void bindMapperForTarget() {
        String targetMapper = builderAssistant.getCurrentTargetMapper();
        if (targetMapper == null) {
            return;
        }
        Class<?> boundType;
        try {
            boundType = Resources.classForName(targetMapper);
        } catch (ClassNotFoundException e) {
            // 忽略，绑定类型不是必需的?
            return;
        }
        if (boundType == null || configuration.hasMapper(boundType)) {
            return;
        }
        // Spring 可能不知道真正的资源名称
        // 所以设置了一个标志来防止从 mapper 接口再次加载这个资源，
        configuration.addLoadedResource("namespace:" + targetMapper);
        configuration.addMapper(boundType);
    }


}
