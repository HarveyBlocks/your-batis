package org.harvey.batis.reflection.property;


import org.harvey.batis.exception.reflection.ReflectionException;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * 一个字段, 如果其含有Getter或Setter, 则认为它是Property
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-24 14:00
 */
public final class FieldProperties {

    private FieldProperties() {
        // 静态方法类
    }

    public static String methodToProperty(String name) {
        if (name.startsWith("is")) {
            name = name.substring(2);
        } else if (name.startsWith("get") || name.startsWith("set")) {
            name = name.substring(3);
        } else {
            throw new ReflectionException("Error parsing property name '" + name + "'.  Didn't start with 'is', 'get' or 'set'.");
        }

        // 是否将第一个字母变成小写
        if (name.length() == 1) {
            name = name.toLowerCase(Locale.ENGLISH);
        } else if (name.length() > 1 && Character.isLowerCase(name.charAt(1))) {
            // 第二个字符不是大写,则进行转换?
            // getURL->URL->URL
            // getA2b->A2b->?a2b/A2b?
            name = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
        }

        return name;
    }

    public static boolean isProperty(Method method) {
        return isGetter(method) || isSetter(method);
    }

    public static boolean isGetter(Method method) {
        String name = method.getName();
        return method.getParameterTypes().length == 0 &&
                ((name.startsWith("get") && name.length() > 3) ||
                        (name.startsWith("is") && name.length() > 2));
    }

    public static boolean isSetter(Method method) {
        String name = method.getName();
        return method.getParameterTypes().length == 1 &&
                name.startsWith("set") &&
                name.length() > 3;
    }

}