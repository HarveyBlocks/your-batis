package org.harvey.batis.util;

import junit.framework.TestCase;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

public class UrlBuilderTest extends TestCase {

    public void testToUrl() {
        String protocol = "http";
        String filepath = "test.html";
        String host = "www.baidu.com";
        String port = "8848";
        Properties args = new Properties();
        args.setProperty("A", "a");
        args.setProperty("B", "b");
        URL url;
        UrlBuilder urlBuilder = new UrlBuilder()
                .setProtocol(protocol)
                .setHost(host)
                .setPort(Integer.valueOf(port))
                .setFilepath(filepath)
                .setQueryParameters(args)
                .setFragment("fragment")
                ;
        try {
            url = urlBuilder.toUrl();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("url = " + url.toString());
        try {
            System.out.println("uri = " + url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        System.out.println("ExternalForm = " + url.toExternalForm());
        System.out.println("protocol = " + url.getProtocol());
        System.out.println("file = " + url.getFile());
        System.out.println("query = " + url.getQuery());
        System.out.println("path = " + url.getPath());
        System.out.println("host = " + url.getHost());
        System.out.println("port = " + url.getPort());
        System.out.println("authority = " + url.getAuthority());
        System.out.println("ref = " + url.getRef());
        try {
            System.out.println("url = " + new URL(urlBuilder.toString()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}