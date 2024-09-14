package org.harvey.batis.spring.autoconfiguration;


import org.harvey.batis.io.Resources;
import org.harvey.batis.session.SqlSessionFactory;
import org.harvey.batis.session.SqlSessionFactoryBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@EnableConfigurationProperties(YourBatisProperties.class)//启用这个连接配置文件的类
@ConditionalOnClass(SqlSessionFactory.class)
public class YourBatisSpringBootAutoconfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "yourbatisSqlSessionFactory")
    public SqlSessionFactory yourbatisSqlSessionFactory(YourBatisProperties properties) throws ReflectiveOperationException {
        String resource = properties.getConfigFile();//mybatis核心配置文件
        InputStream inputStream;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new SqlSessionFactoryBuilder().build(inputStream);
    }
}
