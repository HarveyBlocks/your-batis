package org.harvey.batis.datasource;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
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

    @Test
    public void parseUrl() {
        try {
            URL url = new URL("https","redis",8080,"/employee?k=v");
            System.out.println(url.toURI());
            String query = url.getQuery();
            System.out.println(query);
            System.out.println(url.toString());
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void setProperties() {
    }

    @Test
    public void getDataSource() {
    }
}