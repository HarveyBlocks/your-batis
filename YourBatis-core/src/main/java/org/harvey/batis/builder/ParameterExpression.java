package org.harvey.batis.builder;

import org.harvey.batis.exception.UnfinishedFunctionException;
import org.harvey.batis.exception.builder.BuilderException;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-21 01:57
 */
public class ParameterExpression extends HashMap<String, String> {
    public static final char SEPARATOR = ',';
    public static final char COLON = ':';
    public static final char EQUAL = '=';

    private static final class CharacterSet extends HashSet<Character> {
        public CharacterSet(Collection<? extends Character> c) {
            super(c);
        }

        public CharacterSet(Character... cs) {
            this(Arrays.asList(cs));
        }
    }

    /**
     * @see #SEPARATOR
     */
    private static final CharacterSet SEPARATOR_SET = new CharacterSet(SEPARATOR);
    /**
     * @see #SEPARATOR
     * @see #COLON
     */
    private static final CharacterSet SEPARATOR_COLON_SET = new CharacterSet(SEPARATOR, COLON);
    /**
     * @see #EQUAL
     */
    private static final CharacterSet EQUAL_SET = new CharacterSet(EQUAL);

    public ParameterExpression(String expression) {
        this.parse(expression);
    }

    private void parse(String expression) {
        int position = ParameterExpression.skipWhite(expression, 0);
        if (expression.charAt(position) == '(') {
            throw new UnfinishedFunctionException(expression, position + 1);
            // expression(expression, position + 1);
        } else {
            this.property(expression, position);
        }
    }

    private void property(String expression, int left) {
        if (left >= expression.length()) {
            return;
        }
        // 找","或":"符号
        int right = ParameterExpression.find(expression, left, SEPARATOR_COLON_SET);
        // ","或":"之前的是property
        this.put("property", ParameterExpression.trimmedStr(expression, left, right));
        this.jdbcTypeOpt(expression, right);
    }


    private void jdbcTypeOpt(String expression, int position) {
        position = ParameterExpression.skipWhite(expression, position);
        if (position >= expression.length()) {
            return;
        }
        if (expression.charAt(position) == COLON) {
            // 找到的是":"
            // 其之后到","为止的javaType
            this.jdbcType(expression, position + 1);
        } else if (expression.charAt(position) == SEPARATOR) {
            // 找到的是","
            // 其之后的是option
            option(expression, position);
        } else {
            throw new BuilderException("Parsing error in {" + expression + "} in position " + position);
        }
    }

    /**
     * @param position ({@link #COLON})之后, 到{@link #SEPARATOR}为止的是javaType
     */
    private void jdbcType(String expression, int position) {
        int left = ParameterExpression.skipWhite(expression, position);
        int right = ParameterExpression.find(expression, left, SEPARATOR_SET);
        if (right <= left) {
            throw new BuilderException("Parsing error in {" + expression + "} in position " + position);
        }
        this.put("jdbcType", ParameterExpression.trimmedStr(expression, left, right));
        option(expression, position);
    }

    /**
     * @param position 之后开始寻找, 符合"key=value"的是为option, 存入map.
     *                 只要有, 就不停找有键值对结构的
     */
    private void option(String expression, int position) {
        while (position < expression.length()) {
            position = this.option0(expression, position + 1);
        }
    }

    /**
     * @param position 之后开始寻找, 符合"key=value"的是为option, 存入map
     * @return 找寻的终点
     */
    private int option0(String expression, int position) {
        int left = ParameterExpression.skipWhite(expression, position);
        if (left >= expression.length()) {
            return left;
        }
        int right = ParameterExpression.find(expression, left, EQUAL_SET);
        String name = trimmedStr(expression, left, right);
        left = right + 1;
        right = ParameterExpression.find(expression, left, SEPARATOR_SET);
        String value = trimmedStr(expression, left, right);
        this.put(name, value);
        // 往后寻找
        return right;
    }

    /**
     * @param startIndex 从此开始, 跳过空白符
     * @return 第一个不是空白符的字符, 如果完了就返回expression.length
     */
    private static int skipWhite(String expression, int startIndex) {
        for (int i = startIndex; i < expression.length(); i++) {
            if (expression.charAt(i) > 0x20) {
                return i;
            }
        }
        return expression.length();
    }

    /**
     * @param startIndex 从此开始, 寻找符号
     * @param in         如果expression中包含in中的字符, 就返回所在索引
     * @return 第一个在in集合中的字符, 如果完了还没找到就返回expression.length
     */
    private static int find(String expression, int startIndex, final CharacterSet in) {
        for (int i = startIndex; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (in.contains(c)) {
                return i;
            }
        }
        return expression.length();
    }

    /**
     * 把start和end中间的区域看作新字符串, 去头尾的空白字符
     *
     * @param start 包含
     * @param end   不包含
     * @return str[start:end]的去头尾后的子串
     */
    private static String trimmedStr(String str, int start, int end) {
        while (str.charAt(start) <= 0x20) {
            start++;
        }
        while (str.charAt(end - 1) <= 0x20) {
            end--;
        }
        return start >= end ? "" : str.substring(start, end);
    }
}

