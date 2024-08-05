package org.harvey.batis.datasource;

import org.junit.Test;

import java.util.Properties;

public class UnpooledDataSourceFactoryTest {

    @Test
    public void testSetProperties() {
        UnpooledDataSourceFactory factory = new UnpooledDataSourceFactory();
        Properties properties = new Properties();
        properties.setProperty("driver.","AAA");
        properties.setProperty("driver.1","BBB");
        properties.setProperty("driver.12","CCC");
        factory.setProperties(properties);
    }

    @Test
    public void testGetDataSource() {
    }
}