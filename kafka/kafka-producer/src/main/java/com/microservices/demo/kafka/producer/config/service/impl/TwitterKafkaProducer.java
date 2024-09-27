package com.microservices.demo.kafka.producer.config.service.impl;

import com.microservices.demo.kafka.avro.model.TwitterAvroModel;
import com.microservices.demo.kafka.producer.config.service.KafkaProducer;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class TwitterKafkaProducer implements KafkaProducer<Long, TwitterAvroModel> {
    private static final Logger LOG = LoggerFactory.getLogger(TwitterKafkaProducer.class);
    private final KafkaTemplate<Long, TwitterAvroModel> kafkaTemplate;

    public TwitterKafkaProducer(KafkaTemplate<Long, TwitterAvroModel> template) {
        this.kafkaTemplate = template;
    }

    private static void addCallback(String topicName, TwitterAvroModel message, CompletableFuture<SendResult<Long, TwitterAvroModel>> kafkaResultFuture) {
        kafkaResultFuture.thenAccept(result -> {
            RecordMetadata metadata = result.getRecordMetadata();
            LOG.debug("Received new metadata. Topic: {}; Partition: {}; Offset: {}; Timestamp: {}, at time {}",
                    metadata.topic(), metadata.partition(), metadata.offset(), metadata.timestamp(), System.nanoTime());
        });

        kafkaResultFuture.exceptionally(e -> {
            LOG.error("Error while sending message {} to topic {}", message.toString(), topicName, e);

            return null;
        });
    }


    @Override
    public void send(String topicName, Long key, TwitterAvroModel message) {
        LOG.info("Sending message='{} to topic='{}'", message, topicName);

        CompletableFuture<SendResult<Long, TwitterAvroModel>> kafkaResultFuture = kafkaTemplate.send(topicName, key, message);

        addCallback(topicName, message, kafkaResultFuture);
    }

    @PreDestroy
    public void close() {
        if (kafkaTemplate != null) {
            LOG.info("Closing kafka producer!");

            /* Normally spring closes this kafkaTemplate prior to shutdown, we just call destroy() explicitly to be sure that
            it is destroyed successfully before application shutdown. */
            kafkaTemplate.destroy();
        }
    }
}
