package org.harvey.batis.reflection.property;

import lombok.Getter;

import java.util.Iterator;

/**
 * properties文件的解析工具类, 同时起到存储可Getter/Setter的字段的作用
 * school.students[12].score.math[2].value
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-24 13:02
 */
@Getter
public class PropertyTokenizer implements Iterator<PropertyTokenizer> {
    /**
     * 简单的单独的配置名
     */
    private String name;
    /**
     * 如果有索引, 这个值是带有索引的配置名
     */
    private final String indexedName;
    private String index;
    private final String childrenFullname;

    public PropertyTokenizer(String fullname) {
        // delimiter 界定符
        int delimiter = fullname.indexOf('.'); // 最前面的元素
        // school.students[12].score.math[2].value
        if (delimiter < 0) {
            name = fullname; // 没有限定符".", 则该名字就是配置名value
            childrenFullname = null;
        } else {
            name = fullname.substring(0, delimiter);// 配置的限定school
            childrenFullname = fullname.substring(delimiter + 1);//students[12].score.math[2].value
        }
        // 检查配置是否是数组, 形式如students[12], 也有可能是student[李四]
        indexedName = name; // 就是students[12]
        delimiter = name.indexOf('[');
        if (delimiter >= 0) {
            // 如果配置是数组, 那么解析这个数组
            index = name.substring(delimiter + 1, name.length() - 1); // 中括号里的部分12
            name = name.substring(0, delimiter); // 参数名字变students[12]为students
        }
    }

    @Override
    public boolean hasNext() {
        return childrenFullname != null;
    }

    @Override
    public PropertyTokenizer next() {
        return new PropertyTokenizer(childrenFullname);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove is not supported, as it has no meaning in the context of properties.");
    }
}
