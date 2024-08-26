package org.harvey.batis.builder.xml;

import org.harvey.batis.builder.BaseBuilder;
import org.harvey.batis.builder.MapperBuilderAssistant;
import org.harvey.batis.config.Configuration;
import org.harvey.batis.executor.key.generator.Jdbc3KeyGenerator;
import org.harvey.batis.executor.key.generator.KeyGenerator;
import org.harvey.batis.executor.key.generator.NoKeyGenerator;
import org.harvey.batis.executor.key.generator.SelectKeyGenerator;
import org.harvey.batis.mapping.sqlsource.SqlSource;
import org.harvey.batis.parsing.MapperXmlConstants;
import org.harvey.batis.parsing.XNode;
import org.harvey.batis.scripting.LanguageDriver;
import org.harvey.batis.util.enums.SqlCommandType;

import java.util.Locale;

/**
 * TODO
 * SQL节点
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-04 16:03
 */
public class XMLStatementBuilder extends BaseBuilder {
    private final MapperBuilderAssistant builderAssistant;
    /**
     * SQL节点
     */
    private final XNode context;

    public XMLStatementBuilder(Configuration configuration, MapperBuilderAssistant builderAssistant, XNode context, String requiredDatabaseId) {
        super(configuration);
        this.builderAssistant = builderAssistant;
        this.context = context;
    }

    /**
     * 解析当前节点
     */
    public void parseStatementNode() {
        String methodName = context.getAttributeValue(MapperXmlConstants.Sql.ID_ATTRIBUTION);

        String nodeName = context.getNode().getNodeName();
        SqlCommandType sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase(Locale.ENGLISH));
        /*TODO Include节点 Include Fragments before parsing
        XMLIncludeTransformer includeParser = new XMLIncludeTransformer(configuration, builderAssistant);
        includeParser.applyIncludes(context.getNode());*/
        LanguageDriver langDriver = this.getLanguageDriver();

        KeyGenerator keyGenerator;
        String keyStatementId = methodName + SelectKeyGenerator.SELECT_KEY_SUFFIX;
        keyStatementId = builderAssistant.applyCurrentNamespace(keyStatementId, true);
        if (configuration.hasKeyGenerator(keyStatementId)) {
            keyGenerator = configuration.getKeyGenerator(keyStatementId);
        } else {
            String defaultUseGeneratedKey = "" + (
                    configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType));
            keyGenerator = Boolean.parseBoolean(
                    context.getAttributeValue("useGeneratedKeys", defaultUseGeneratedKey)
            ) ? Jdbc3KeyGenerator.INSTANCE : NoKeyGenerator.INSTANCE;
        }


        SqlSource sqlSource = langDriver.createSqlSource(configuration, context, null);
        // Parse the SQL (pre: <selectKey> and <include> were parsed and removed)
        String resultType = context.getAttributeValue(MapperXmlConstants.Sql.RESULT_TYPE_ATTRIBUTION);
        Class<?> resultTypeClass = resolveClass(resultType);
        String resultMap = context.getAttributeValue(MapperXmlConstants.Sql.RESULT_MAP_ATTRIBUTION);
        builderAssistant.addMappedStatement(methodName, sqlSource, sqlCommandType, resultMap, resultTypeClass, langDriver, keyGenerator);
    }

    private LanguageDriver getLanguageDriver() {
        return configuration.getLanguageDriver();
    }

}
