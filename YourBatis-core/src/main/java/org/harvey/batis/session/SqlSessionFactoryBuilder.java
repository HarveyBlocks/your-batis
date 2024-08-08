package org.harvey.batis.session;

import org.harvey.batis.builder.xml.XMLConfigBuilder;
import org.harvey.batis.config.Configuration;
import org.harvey.batis.exception.ExceptionFactory;
import org.harvey.batis.util.ErrorContext;
import org.xml.sax.InputSource;

import java.io.Closeable;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

/**
 * 创建SqlSessionFactory的建造者
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-02 12:32
 * @see SqlSessionFactory
 */
public class SqlSessionFactoryBuilder {
    /**
     * @see #build(Reader, String, Properties)
     */
    public SqlSessionFactory build(Reader reader) {
        return build(reader, null, null);
    }

    /**
     * @see #build(Reader, String, Properties)
     */
    public SqlSessionFactory build(Reader reader, String environment) {
        return build(reader, environment, null);
    }

    /**
     * @see #build(Reader, String, Properties)
     */
    public SqlSessionFactory build(Reader reader, Properties properties) {
        return build(reader, null, properties);
    }

    /**
     * @param reader 用于组成{@link InputSource}
     * @see #build(Closeable, InputSource, String, Properties)
     */
    public SqlSessionFactory build(Reader reader, String environment, Properties properties) {
        return this.build(reader, new InputSource(reader), environment, properties);
    }

    /**
     * @see #build(InputStream, String, Properties)
     */
    public SqlSessionFactory build(InputStream inputStream) {
        return build(inputStream, null, null);
    }

    /**
     * @see #build(InputStream, String, Properties)
     */
    public SqlSessionFactory build(InputStream inputStream, String environment) {
        return this.build(inputStream, environment, null);
    }

    /**
     * @see #build(InputStream, String, Properties)
     */
    public SqlSessionFactory build(InputStream inputStream, Properties properties) {
        return build(inputStream, null, properties);
    }

    /**
     * @param inputStream 用于组成{@link InputSource}
     * @see #build(Closeable, InputSource, String, Properties)
     */
    public SqlSessionFactory build(InputStream inputStream, String environment, Properties properties) {
        return build(inputStream, new InputSource(inputStream), environment, properties);
    }

    /**
     * @param closeable 必须是{@param source}的本体(reader或stream)
     * @see XMLConfigBuilder
     * @see XMLConfigBuilder#XMLConfigBuilder(Reader, String, Properties)
     * @see #build(Configuration)
     */
    private SqlSessionFactory build(Closeable closeable, InputSource source, String environment, Properties properties) {
        try (closeable/*故意忽略read.close()的异常。首选上一个错误。*/) {
            XMLConfigBuilder parser = new XMLConfigBuilder(source, environment, properties);
            return this.build(parser.parse());
        } catch (Exception e) {
            // 创建SqlSession时发生异常
            throw ExceptionFactory.wrapException("Error building SqlSession.", e);
        } finally {
            // 重启错误日志上下文: 一整个build的过程看作一个错误上下文的流程, 已结束
            ErrorContext.instance().reset();
        }
    }

    /**
     * 返回{@link DefaultSqlSessionFactory}
     */
    public SqlSessionFactory build(Configuration config) {
        return new DefaultSqlSessionFactory(config);
    }

}
