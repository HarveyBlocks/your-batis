package org.harvey.batis.util;

import junit.framework.TestCase;
import org.junit.Assert;

public class StrictMapTest extends TestCase {

    public void testGet() {
        StrictMap<Integer> name = new StrictMap<>("name");
        name.put("key.value", 12);
        name.put("int.value", 24);
        System.out.println(name.get("int.value"));
        Assert.assertThrows(IllegalArgumentException.class, () -> {
            name.get("value");
        });
    }
}