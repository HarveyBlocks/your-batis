package org.harvey.batis.datasource;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class PooledDataSourceTest extends TestCase {

    private static final PooledDataSource DATA_SOURCE = new PooledDataSource();

    public void testForceCloseAll() {
        List<String> sourceList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            sourceList.add(String.valueOf(i));
        }
        List<Thread> threadList = PooledDataSourceTest.prepareThreads(() -> {
            for (int i = sourceList.size(); i > 0; i--) {
                String s = sourceList.remove(i - 1);
                System.out.println(s);
            }
        }, "Thread");
        for (int i = 0; i < 20; i++) {
            threadList.get(i).start();
        }
    }

    private static List<Thread> prepareThreads(Runnable task, String namePre) {
        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            threadList.add(new Thread(task, namePre + "-" + i));
        }
        return threadList;
    }

    public void testPushConnection() {
    }

    int count;

    class InnerTask implements Runnable {

        private final Object lock;

        InnerTask(Object lock) {
            this.lock = lock;
        }

        @Override
        public void run() {
            for (int i = 0; i < 20; i++) {
                synchronized (lock) {
                    try {

                        lock.wait(1000);
                        System.out.println(Thread.currentThread().getName() + ":" + count++);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    public void testGetConnect() {
        Object lock1 = new Object();
        Object lock2 = new Object();
        List<Thread> threadList1 = PooledDataSourceTest.prepareThreads(new InnerTask(lock1), "lock1");
        List<Thread> threadList2 = PooledDataSourceTest.prepareThreads(new InnerTask(lock2), "lock2");
        for (int i = 0; i < threadList2.size(); i++) {
            threadList1.get(i).start();
            threadList2.get(i).start();
        }
    }

    public static void main(String[] args) {
        new PooledDataSourceTest().testGetConnect();
    }
}