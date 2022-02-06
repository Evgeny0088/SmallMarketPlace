package com.marketPlace.Orders.configs;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaProducerConfig {

    private static final String TOPIC_REQUEST_FOR_DELETION = "request_for_deletion";

    @Bean
    public NewTopic requestForDeletion(){
        return TopicBuilder
                .name(TOPIC_REQUEST_FOR_DELETION)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
