package org.harvey.batis;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 用于测试的目标类
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-02 16:06
 */
@Data
@NoArgsConstructor
public class TestTargetObject {
    private String name;
    private int level;
    public int publicField;

    public TestTargetObject(String name, int level) {
        this.name = name;
        this.level = level;
    }

    protected String testProtected(int ignore) {
        return this.getClass().getName();
    }

    public String testPublic(int ignore) {
        return this.getClass().getName();
    }

    public String concatName(String name) {
        this.name = this.name + ", " + name;
        return this.name;
    }

    public int concatLevel(int level) {
        this.level |= level;
        return this.level;
    }
}
