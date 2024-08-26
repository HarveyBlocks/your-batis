package org.harvey.batis.util;

import java.util.*;

/**
 * 提供可以处理包括数组在内的 hashCode、equals 和 toString 方法。
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-14 21:15
 */
public class ArrayUtil {
    public static String[] split(String str, char delimiter) {
        return split(str, "" + delimiter);
    }

    /**
     * @param delimiters 分隔符的集合
     */
    public static String[] split(String str, String delimiters) {
        StringTokenizer st = new StringTokenizer(str, delimiters, false);
        String[] array = new String[st.countTokens()];
        for (int i = 0; st.hasMoreTokens(); i++) {
            array[i] = st.nextToken();
        }
        return array;
    }

    public static String[] splitTrimEach(String str, char delimiter) {
        return splitTrimEach(str, "" + delimiter);
    }

    /**
     * 每个元素都trim, 然后如果是空字符串就不加入结果
     *
     * @param delimiter 一个分隔符字符串
     */
    public static String[] splitTrimEach(String src, String delimiter) {
        if (src == null) {
            return null;
        }
        src = src.trim();
        if (src.isEmpty()) {
            return null;
        }
        int index;
        List<String> result = new ArrayList<>();
        while ((index = src.indexOf(delimiter)) >= 0) {
            String trim = src.substring(0, index).trim();
            if (!trim.isEmpty()) {
                result.add(trim);
            }
            src = src.substring(index + 1);
        }
        String trim = src.trim();
        if (!trim.isEmpty()) {
            result.add(trim);
        }
        return result.toArray(new String[0]);
    }


    public static int splitCount(String str, char delimiter) {
        return splitCount(str, "" + delimiter);
    }

    public static int splitCount(String str, String dim) {
        StringTokenizer st = new StringTokenizer(str, dim, false);
        return st.countTokens();
    }

    /**
     * @return 如果是Object的情况就普通hashcode, 否则 {@link Arrays#hashCode(int[])} 等
     */
    public static int hashCode(Object obj) {
        if (obj == null) {
            // for consistency with Arrays#hashCode() and Objects#hashCode()
            return 0;
        }
        final Class<?> clazz = obj.getClass();
        if (!clazz.isArray()) {
            return obj.hashCode();
        }
        final Class<?> componentType = clazz.getComponentType();
        if (long.class.equals(componentType)) {
            return Arrays.hashCode((long[]) obj);
        } else if (int.class.equals(componentType)) {
            return Arrays.hashCode((int[]) obj);
        } else if (short.class.equals(componentType)) {
            return Arrays.hashCode((short[]) obj);
        } else if (char.class.equals(componentType)) {
            return Arrays.hashCode((char[]) obj);
        } else if (byte.class.equals(componentType)) {
            return Arrays.hashCode((byte[]) obj);
        } else if (boolean.class.equals(componentType)) {
            return Arrays.hashCode((boolean[]) obj);
        } else if (float.class.equals(componentType)) {
            return Arrays.hashCode((float[]) obj);
        } else if (double.class.equals(componentType)) {
            return Arrays.hashCode((double[]) obj);
        } else {
            return Arrays.hashCode((Object[]) obj);
        }
    }

    /**
     * @return 如果是Object的情况就普通比较, 否则 {@link Arrays#equals(int[], int[])}等
     */
    public static boolean equals(Object thisObj, Object thatObj) {
        if (thisObj == null) {
            return thatObj == null;
        }
        // 此时 thisObj != null
        if (thatObj == null) {
            return false;
        }
        final Class<?> thisClass = thisObj.getClass();
        if (!thisClass.equals(thatObj.getClass())) {
            return false;
        }
        // 保证两个类型一致
        if (!thisClass.isArray()) {
            return thisObj.equals(thatObj);
        }
        // 保证两个类型都是相同的数组类型
        final Class<?> componentType = thisClass.getComponentType();
        if (long.class.equals(componentType)) {
            return Arrays.equals((long[]) thisObj, (long[]) thatObj);
        } else if (int.class.equals(componentType)) {
            return Arrays.equals((int[]) thisObj, (int[]) thatObj);
        } else if (short.class.equals(componentType)) {
            return Arrays.equals((short[]) thisObj, (short[]) thatObj);
        } else if (char.class.equals(componentType)) {
            return Arrays.equals((char[]) thisObj, (char[]) thatObj);
        } else if (byte.class.equals(componentType)) {
            return Arrays.equals((byte[]) thisObj, (byte[]) thatObj);
        } else if (boolean.class.equals(componentType)) {
            return Arrays.equals((boolean[]) thisObj, (boolean[]) thatObj);
        } else if (float.class.equals(componentType)) {
            return Arrays.equals((float[]) thisObj, (float[]) thatObj);
        } else if (double.class.equals(componentType)) {
            return Arrays.equals((double[]) thisObj, (double[]) thatObj);
        } else {
            return Arrays.equals((Object[]) thisObj, (Object[]) thatObj);
        }
    }

    /**
     * @return 如果是Object的情况就普通toString, 否则 {@link Arrays#toString(int[])}等
     */
    public static String toString(Object obj) {
        if (obj == null) {
            return "null";
        }
        final Class<?> clazz = obj.getClass();
        if (!clazz.isArray()) {
            return obj.toString();
        }
        final Class<?> componentType = obj.getClass().getComponentType();
        if (long.class.equals(componentType)) {
            return Arrays.toString((long[]) obj);
        } else if (int.class.equals(componentType)) {
            return Arrays.toString((int[]) obj);
        } else if (short.class.equals(componentType)) {
            return Arrays.toString((short[]) obj);
        } else if (char.class.equals(componentType)) {
            return Arrays.toString((char[]) obj);
        } else if (byte.class.equals(componentType)) {
            return Arrays.toString((byte[]) obj);
        } else if (boolean.class.equals(componentType)) {
            return Arrays.toString((boolean[]) obj);
        } else if (float.class.equals(componentType)) {
            return Arrays.toString((float[]) obj);
        } else if (double.class.equals(componentType)) {
            return Arrays.toString((double[]) obj);
        } else {
            return Arrays.toString((Object[]) obj);
        }
    }
}
