package org.harvey.batis.session;

import org.harvey.batis.builder.xml.XMLConfigBuilder;
import org.harvey.batis.config.Configuration;
import org.harvey.batis.exception.ExceptionFactory;
import org.harvey.batis.exception.UnfinishedFunctionException;
import org.harvey.batis.util.ErrorContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-02 12:32
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
     * TODO
     */
    public SqlSessionFactory build(Reader reader, String environment, Properties properties) {
        try (reader/*故意忽略read.close()的异常。首选上一个错误。*/) {
            XMLConfigBuilder parser = new XMLConfigBuilder(reader, environment, properties);
            return build(parser.parse());
        } catch (Exception e) {
            // 创建SqlSession时发生异常
            throw ExceptionFactory.wrapException("Error building SqlSession.", e);
        } finally {
            ErrorContext.instance().reset();
        }

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
        return build(inputStream, environment, null);
    }

    /**
     * @see #build(InputStream, String, Properties)
     */
    public SqlSessionFactory build(InputStream inputStream, Properties properties) {
        return build(inputStream, null, properties);
    }

    /**
     * TODO
     */
    public SqlSessionFactory build(InputStream inputStream, String environment, Properties properties) {
        throw new UnfinishedFunctionException(inputStream, environment, properties);
    }

    /**
     * {@link DefaultSqlSessionFactory#DefaultSqlSessionFactory(Configuration)}
     */
    public SqlSessionFactory build(Configuration config) {
        return new DefaultSqlSessionFactory(config);
    }

}
