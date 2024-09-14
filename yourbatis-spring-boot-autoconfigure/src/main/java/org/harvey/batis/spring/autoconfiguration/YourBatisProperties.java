package org.harvey.batis.spring.autoconfiguration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@ConfigurationProperties(prefix = "redis")
// 在属性文件(application{-profiles}.properties等)以"redis"开头的属性将封装成这个类
public class YourBatisProperties {
    private String configFile = "yourbatis-config.xml";
}
