package com.microservices.demo.kafka.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    /* Instead of creating the web client each time, we want to create a spring bean and reuse it each time when we need
     a web client. */
    @Bean
    WebClient webClient() {
        return WebClient.builder().build();
    }
}
