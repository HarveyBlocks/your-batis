package org.harvey.batis.demo;


import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;

public class AppTest {
    @Test
    public void test() throws IOException {
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources("org/harvey/batis/demo/mapper");
        Iterator<URL> iterator = resources.asIterator();
        ArrayList<URL> list = Collections.list(resources);
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
        for (URL url : list) {
            System.out.println(url);
        }
    }
}
