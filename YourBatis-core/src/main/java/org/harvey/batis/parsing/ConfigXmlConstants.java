package org.harvey.batis.parsing;

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
public interface ConfigXmlConstants {
    String XSD_FILENAME = "yourbatis-config.xsd";
    String TRANSACTION_MANAGER_ELEMENT = "transactionManager";

    String NAMESPACE_PREFIX = "cfg";
    String NAMESPACE_URI = "http://batis.harvey.org/schema/config";
    NamespaceContext CONFIG_NAMESPACE_CONTEXT = new NamespaceContext() {
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
    String CONFIG_ELEMENT = "config";
    String DATABASE_ELEMENT = "database";
    String MAPPERS_ELEMENT = "mappers";
    String MAPPERS_PATH_ATTRIBUTION = "path";
    String DRIVER_CLASS_ATTRIBUTION = "driverClassName";
    String DATABASE_URL_ELEMENT = "url";

    String DATABASE_URL_PROTOCOL_ATTRIBUTION = "protocol";
    String DATABASE_URL_DATABASE_NAME_ELEMENT = "database-name";
    String DATABASE_URL_HOST_ELEMENT = "host";
    String DATABASE_URL_PORT_ELEMENT = "port";
    String DATABASE_URL_SSL_ATTRIBUTION = "useSSL";
    String DATABASE_URL_CHARSET_ATTRIBUTION = "characterEncoding";
    String DATABASE_URL_UNICODE_ATTRIBUTION = "useUnicode";
    String DATABASE_URL_TIMEZONE_ATTRIBUTION = "serverTimezone";
    String DATABASE_AUTH_ELEMENT = "auth";
    String DATABASE_AUTH_USER_ELEMENT = "username";
    String DATABASE_AUTH_PWD_ELEMENT = "password";
    String DATASOURCE_ELEMENT = "datasource";
    String DATASOURCE_INITIAL_SIZE_ELEMENT = "initialSize";
    String DATASOURCE_MAX_ACTIVE_ELEMENT = "maxActive";
    String DATASOURCE_MAX_WAITING_MILLION_ELEMENT = "maxWaitMillion";
    String ROOT_ELEMENT = CONFIG_ELEMENT;


    String PROPERTIES_ELEMENT = "properties";
    String RESOURCE_ELEMENT = "resource";
    String FILEPATH_ATTRIBUTION = "filepath";
    String PROPERTY_ELEMENT = "property";
    String KEY_ATTRIBUTION = "key";
    String VALUE_ATTRIBUTION = "value";
}
