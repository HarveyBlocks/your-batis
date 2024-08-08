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
public class ConfigXmlConstants {
    public static final String TRANSACTION_MANAGER_ELEMENT = "transactionManager";

    private ConfigXmlConstants() {
    }

    public static final String NAMESPACE_PREFIX = "cfg";
    public static final String NAMESPACE_URI = "http://batis.harvey.org/schema/config";
    public static final NamespaceContext CONFIG_NAMESPACE_CONTEXT = new NamespaceContext() {
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
    public static final String CONFIG_ELEMENT = "config";
    public static final String DATABASE_ELEMENT = "database";
    public static final String MAPPERS_ELEMENT = "mappers";
    public static final String MAPPERS_PATH_ATTRIBUTION = "path";
    public static final String DRIVER_CLASS_ATTRIBUTION = "driverClassName";
    public static final String DATABASE_URL_ELEMENT = "url";

    public static final String DATABASE_URL_PROTOCOL_ATTRIBUTION = "protocol";
    public static final String DATABASE_URL_DATABASE_NAME_ELEMENT = "database-name";
    public static final String DATABASE_URL_HOST_ELEMENT = "host";
    public static final String DATABASE_URL_PORT_ELEMENT = "port";
    public static final String DATABASE_URL_SSL_ATTRIBUTION = "useSSL";
    public static final String DATABASE_URL_CHARSET_ATTRIBUTION = "characterEncoding";
    public static final String DATABASE_URL_UNICODE_ATTRIBUTION = "useUnicode";
    public static final String DATABASE_URL_TIMEZONE_ATTRIBUTION = "serverTimezone";
    public static final String DATABASE_AUTH_ELEMENT = "auth";
    public static final String DATABASE_AUTH_USER_ELEMENT = "username";
    public static final String DATABASE_AUTH_PWD_ELEMENT = "password";
    public static final String DATASOURCE_ELEMENT = "datasource";
    public static final String DATASOURCE_INITIAL_SIZE_ELEMENT = "initialSize";
    public static final String DATASOURCE_MAX_ACTIVE_ELEMENT = "maxActive";
    public static final String DATASOURCE_MAX_WAITING_MILLION_ELEMENT = "maxWaitMillion";
    public static final String ROOT_ELEMENT = CONFIG_ELEMENT;


}
