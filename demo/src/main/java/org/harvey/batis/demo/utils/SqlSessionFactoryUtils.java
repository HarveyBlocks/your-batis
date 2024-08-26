package org.harvey.batis.demo.utils;


import org.harvey.batis.io.Resources;
import org.harvey.batis.session.SqlSessionFactory;
import org.harvey.batis.session.SqlSessionFactoryBuilder;
import org.harvey.batis.util.type.TypeHandlerRegistry;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-26 11:43
 */
public class SqlSessionFactoryUtils {
    private static SqlSessionFactory sqlSessionFactory;

    static {
        try {
            String resource = "yourbatis-config.xml";//mybatis核心配置文件
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    public static SqlSessionFactory getFactory() {
        return sqlSessionFactory;
    }
}
