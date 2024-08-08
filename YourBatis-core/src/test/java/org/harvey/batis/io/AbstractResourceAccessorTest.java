package org.harvey.batis.io;

import junit.framework.TestCase;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

public class AbstractResourceAccessorTest extends TestCase {

    public void testList() {
    }

    public void testGetResources() throws IOException {
        String[] paths = {
                "org/harvey/batis/util/StrictMapTest.class",
                "org/harvey/batis/util/StrictMapTest", // 不可
                "/org/harvey/batis/util/StrictMapTest.class", // 不可
                "/org/harvey/batis/util/StrictMapTest", // 不可
                "/org/harvey/batis/util/", // 不可
                "/org/harvey/batis/util", // 不可
                "org/harvey/batis/util/StrictMapTest.java", // 不可
                "org/harvey/batis/util/StrictMapTest.properties", // 可
                "org/harvey/batis/util/", // 可
                "org/harvey/batis/util", // 可
        };
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        for (String path : paths) {
            System.out.println("path = " + path);
            Enumeration<URL> resources = contextClassLoader.getResources(path);
            while (resources.hasMoreElements()) {
                System.out.println("nextElement = " + resources.nextElement());
            }
        }
    }

    public void testTestList() {
    }
}