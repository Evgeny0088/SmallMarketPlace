package com.marketplace.itemstorageservice.configs.kafka;

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

    @Value("${itemDTO.topic.name_3}")
    private String SOLD_ITEM_REQUEST;

    @Bean(name = "allPackagesTopic" )
    public NewTopic updateAllPackagesTopic(){
        return TopicBuilder
                .name(TOPIC_UPDATE_ALL_PACKAGES)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean(name = "updateItemRequest")
    public NewTopic updateItemTopic(){
        return TopicBuilder
                .name(TOPIC_UPDATE_ITEM)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean(name = "soldItemRequest")
    public NewTopic soldItemTopic(){
        return TopicBuilder
                .name(SOLD_ITEM_REQUEST)
                .partitions(1)
                .replicas(1)
                .build();
    }
}


