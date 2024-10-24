package com.microservices.demo.twitter.to.kafka.service;

import com.microservices.demo.twitter.to.kafka.service.init.StreamInitializer;
import com.microservices.demo.twitter.to.kafka.service.runner.StreamRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/* @ComponentScan(basePackages = "com.microservices.demo") is required to allow finding the spring beans in other modules. When a spring boot
app starts, by default it scans the packages starting from the package directory that the spring boot app main class resides in.
In twitter-to-kafka-service module, the package that the application class scans, is `com.microservices.demo.twitter.to.kafka.service`.*/
@SpringBootApplication
@ComponentScan(basePackages = "com.microservices.demo")
public class TwitterToKafkaServiceApplication implements CommandLineRunner {
    private static final Logger LOG = LoggerFactory.getLogger(TwitterToKafkaServiceApplication.class);
    private final StreamRunner streamRunner;
    private final StreamInitializer streamInitializer;

    public TwitterToKafkaServiceApplication(StreamRunner streamRunner,
                                            StreamInitializer initializer) {
        this.streamRunner = streamRunner;
        this.streamInitializer = initializer;
    }

    public static void main(String[] args) {
        SpringApplication.run(TwitterToKafkaServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        LOG.info("App starts...");

        /* in kafka implementation, check if kafka topics are created and schema registry is up and running before actually starting streaming
        data from twitter. */
        streamInitializer.init();
        streamRunner.start();
    }
}
