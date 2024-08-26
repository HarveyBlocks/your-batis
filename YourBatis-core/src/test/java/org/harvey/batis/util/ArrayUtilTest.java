package org.harvey.batis.util;

import junit.framework.TestCase;

import java.util.Arrays;

public class ArrayUtilTest extends TestCase {

    public void testSplit() {
        String[] split = ArrayUtil.split("a, a;b,c", ",;");
        System.out.println(Arrays.toString(split));
    }
}