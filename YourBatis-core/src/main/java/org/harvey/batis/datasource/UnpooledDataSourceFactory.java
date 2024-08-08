package org.harvey.batis.datasource;

import org.harvey.batis.exception.datasource.DataSourceException;
import org.harvey.batis.parsing.ConfigXmlConstants;
import org.harvey.batis.reflection.MetaObject;
import org.harvey.batis.reflection.SystemMetaObject;
import org.harvey.batis.util.UrlBuilder;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * 没有使用连接池的DataSource的工厂
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-24 09:46
 */
public class UnpooledDataSourceFactory implements DataSourceFactory {

    private static final String DRIVER_PROPERTY_PREFIX = "driver.";
    private static final int DRIVER_PROPERTY_PREFIX_LENGTH = DRIVER_PROPERTY_PREFIX.length();

    protected final DataSource dataSource;

    public UnpooledDataSourceFactory() {
        this(new UnpooledDataSource());
    }

    protected UnpooledDataSourceFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static String parseUrl(Properties urlProps, Properties args) {
        String protocol = (String) urlProps.get(ConfigXmlConstants.DATABASE_URL_PROTOCOL_ATTRIBUTION);
        String database = (String) urlProps.get(ConfigXmlConstants.DATABASE_URL_DATABASE_NAME_ELEMENT);
        String host = (String) urlProps.get(ConfigXmlConstants.DATABASE_URL_HOST_ELEMENT);
        String port = (String) urlProps.get(ConfigXmlConstants.DATABASE_URL_PORT_ELEMENT);
        UrlBuilder urlBuilder = new UrlBuilder()
                .setProtocol(protocol)
                .setHost(host)
                .setPort(Integer.valueOf(port))
                .setFilepath(database)
                .setQueryParameters(args);
        return urlBuilder.toString();
    }

    /**
     * 为DataSource注入配置
     *
     * @param properties 为DataSource的driverProperties注入, 需要配置以
     *                   {@link UnpooledDataSourceFactory#DRIVER_PROPERTY_PREFIX}为头;
     *                   <br/>否则, 为DataSource的什么字段注入, Key就是该字段的字段名
     */
    @Override
    public void setProperties(Properties properties) {
        Properties driverProperties = new Properties();
        MetaObject metaDataSource = SystemMetaObject.forObject(dataSource);
        for (Object key : properties.keySet()) {
            String propertyName = (String) key;
            if (propertyName.startsWith(DRIVER_PROPERTY_PREFIX)) {
                // 是driver.开头的配置
                String value = properties.getProperty(propertyName);
                String driverKey = propertyName.substring(DRIVER_PROPERTY_PREFIX_LENGTH); // 去除前缀driver
                // 重新存入(?????好没用的功能)
                driverProperties.setProperty(driverKey, value);
            } else if (metaDataSource.hasSetter(propertyName)) {
                // 对于可写的配置, 进行依赖注入
                String value = (String) properties.get(propertyName);
                // 依据获取该配置名, 获取其类型
                Class<?> targetType = metaDataSource.getSetterType(propertyName);
                // 将字符串的源值转换成目标值
                Object convertedValue = convertValue(targetType, value);
                // 依赖注入
                metaDataSource.setValue(propertyName, convertedValue);
            } else {
                throw new DataSourceException("Unknown DataSource property: " + propertyName);
            }
        }
        if (!driverProperties.isEmpty()) {
            // 当存在DriverProperties时, 将字段metaDataSource的值设置为driverProperties
            metaDataSource.setValue("driverProperties", driverProperties);
        }
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * 依据配置名对应的类型, 将value解析
     *
     * @param value      解析的源
     * @param targetType 支持 {@link Integer}{@link Long}{@link Boolean}及其拆箱后的类型;
     *                   <br/>否则, 返回字符串类型的值
     * @return 最终转换后的值
     */
    private Object convertValue(Class<?> targetType, String value) {
        Object convertedValue = value;
        // 根据配置的不同, 获取字符串值在不同类型下的值
        if (targetType == Integer.class || targetType == int.class) {
            convertedValue = Integer.valueOf(value);
        } else if (targetType == Long.class || targetType == long.class) {
            convertedValue = Long.valueOf(value);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            convertedValue = Boolean.valueOf(value);
        }
        return convertedValue;
    }

}
