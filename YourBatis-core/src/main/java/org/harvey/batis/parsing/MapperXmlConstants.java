package org.harvey.batis.parsing;

import org.harvey.batis.mapping.ResultMap;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.Iterator;

/**
 * 关于解析config文件的常量类
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-05 17:01
 */
public interface MapperXmlConstants {


    String XSD_FILENAME = "yourbatis-mapper.xsd";
    String NAMESPACE_PREFIX = "mpr";
    String NAMESPACE_URI = "http://batis.harvey.org/schema/config";
    NamespaceContext MAPPER_NAMESPACE_CONTEXT = new NamespaceContext() {
        public static final String CONFIG = NAMESPACE_URI;

        @Override
        public String getNamespaceURI(String prefix) {
            if (NAMESPACE_PREFIX.equals(prefix)) {
                return CONFIG;
            }
            return XMLConstants.NULL_NS_URI;
        }

        @Override
        public String getPrefix(String namespaceUri) {
            return null;
        }

        @Override
        public Iterator<String> getPrefixes(String namespaceUri) {
            return null;
        }
    };

    String MAPPER_ELEMENT = "mapper";
    String MAPPER_TARGET_ATTRIBUTE = "target";


    String ROOT_ELEMENT = MAPPER_ELEMENT;

    String RESULT_MAP_ELEMENT = "result-map";

    String MAP_ELEMENT_TYPE_HANDLER = "type-handler";

    interface ResultMap {
        String ELEMENT_NAME = RESULT_MAP_ELEMENT;
        String ID_ATTRIBUTION = "id";
        String ENTITY_TYPE_ATTRIBUTION = "java-entity-type";
        String JAVA_TYPE_HANDLER_ATTRIBUTION = "type-handler";
        String MAP_ELEMENT_COLUMN = "table-column";
        String MAP_ELEMENT_FIELD = "java-field";
    }

    interface Sql {

        String SQL_SELECT_ELEMENT = "select";
        String SQL_INSERT_ELEMENT = "insert";
        String SQL_UPDATE_ELEMENT = "update";
        String SQL_DELETE_ELEMENT = "delete";
        String MAPPER_METHOD_ATTRIBUTION = "method-name";
        String ID_ATTRIBUTION = MAPPER_METHOD_ATTRIBUTION;
        String RESULT_MAP_ATTRIBUTION = "result-map";
        char RESULT_MAP_ATTRIBUTION_SEPARATOR = ' ';

        String RESULT_TYPE_ATTRIBUTION = "result-type";
    }

    interface DynamicSql {
        String TRIM_ELEMENT = "trim";
        String WHERE_ELEMENT = "where";
        String SET_ELEMENT = "set";
        String FOREACH_ELEMENT = "foreach";
        String IF_ELEMENT = "if";
        String CHOOSE_ELEMENT = "choose";
        String WHEN_ELEMENT = "when";
        String OTHERWISE_ELEMENT = "otherwise";
        String BOOLEAN_EXPRESSION_ATTRIBUTE = "match";
        String PREFIX_ATTRIBUTION = "prefix";
        String PREFIX_OVERRIDES_ATTRIBUTION = "prefixOverrides";
        String SUFFIX_ATTRIBUTION = "suffix";
        String SUFFIX_OVERRIDES_ATTRIBUTION = "suffixOverrides";

        String COLLECTION_ATTRIBUTION = "collection";
        String ITEM_ATTRIBUTION = "item";
        String INDEX_ATTRIBUTION = "index";
        String OPEN_ATTRIBUTION = "open";
        String CLOSE_ATTRIBUTION = "close";
        String SEPARATOR_ATTRIBUTION = "separator";
        String MATCH_ATTRIBUTION = "match";
    }

}
