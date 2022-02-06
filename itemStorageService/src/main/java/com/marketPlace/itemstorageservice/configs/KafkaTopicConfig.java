package com.marketPlace.itemstorageservice.configs;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${itemDTO.topic.name_1}")
    private String TOPIC_UPDATE_ALL_PACKAGES;

    @Value("${itemDTO.topic.name_2}")
    private String TOPIC_UPDATE_ITEM;

    @Bean
    public NewTopic updateAllPackagesTopic(){
        return TopicBuilder
                .name(TOPIC_UPDATE_ALL_PACKAGES)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic updateItemTopic(){
        return TopicBuilder
                .name(TOPIC_UPDATE_ITEM)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
