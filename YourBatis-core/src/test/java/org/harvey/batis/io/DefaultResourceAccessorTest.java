package org.harvey.batis.io;

import junit.framework.TestCase;
import lombok.SneakyThrows;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.jar.JarInputStream;

public class DefaultResourceAccessorTest extends TestCase {

    @SneakyThrows
    public void testIsValid() throws ClassNotFoundException {
        Class<Integer> integerClass = Integer.class;
        String name = integerClass.getName();
        System.out.println(name);
        Class<?> aClass = Resources.classForName(name);
        System.out.println(aClass);
    }

    public void testList() {
        StringJoiner sj = new StringJoiner(",");
        sj.add("apple");
        sj.add("banana");
        sj.add("orange");
        String result = sj.toString();
        System.out.println(result);
    }

    public void testFindJarForResource() {
        System.out.println(" \n Hell \t\t  \t  o     \t     wor \n    ld  \t \n  "
                .trim()
                // 将中间所有空白符替换为一个空格
                .replaceAll("\\s+", " "));
    }

    int a = 0;
    Object LOCK = new Object();

    public void testIsJar() {
        List<Thread> group = new ArrayList<>();
        Runnable task = () -> {
            int i = 200;
            while (i-- > 0) {
                out(LOCK);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        for (int i = 0; i < 5; i++) {
            group.add(new Thread(task));
        }
        for (int i = 0; i < 5; i++) {
            group.get(i).start();
        }
    }

    private void out(Object lock) {
        synchronized (lock) {
            int x = a++;
            System.out.println(x);
        }
    }

    public static void main(String[] args) {
        new DefaultResourceAccessorTest().testIsJar();
    }

    public void testListResources() throws IOException {
        DefaultResourceAccessor accessor = new DefaultResourceAccessor();
        accessor.listResources(new JarInputStream(new FileInputStream("D:\\IT_study\\source\\JDK\\YourBatis\\demo\\target\\demo-1.0-SNAPSHOT.jar")), "org/harvey/batis/demo");
    }

    public void testList0() {
        DefaultResourceAccessor accessor = new DefaultResourceAccessor();
        String[] paths = {"org/harvey/batis/exception"};
        for (String path : paths) {
            testList(path, accessor);
        }
    }

    private static void testList(String path, DefaultResourceAccessor accessor) {
        try {
            List<URL> resources = Collections.list(
                    Thread.currentThread().getContextClassLoader().getResources(path));
            System.out.println(resources);
            List<String> list = new ArrayList<>();
            for (URL resource : resources) {
                list.addAll(accessor.list(path, resource));
            }
            System.out.println("list = " + list);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}