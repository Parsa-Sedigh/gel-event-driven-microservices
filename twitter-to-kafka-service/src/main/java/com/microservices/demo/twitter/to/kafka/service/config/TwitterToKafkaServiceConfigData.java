package com.microservices.demo.twitter.to.kafka.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/* `prefix` must match the one in application.yml .
Also use @Configuration to make it a spring bean.*/
@Data
@Configuration
@ConfigurationProperties(prefix = "twitter-to-kafka-service")
public class TwitterToKafkaServiceConfigData {
    /* Again, the naming of properties is important here and must match the ones in application.yml . There, we have: `twitter-keyword`.
    So we have to use camel case here.*/
    private List<String> twitterKeywords;
    private String welcomeMessage;
}
