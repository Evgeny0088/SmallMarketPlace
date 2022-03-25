package com.marketplace.orders.configs.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Qualifier;
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
    private String TOPIC_REQUEST_FOR_DELETION;

    @Bean(name = "allPackagesTopic")
    public NewTopic updateAllPackagesTopic(){
        return TopicBuilder
                .name(TOPIC_UPDATE_ALL_PACKAGES)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean(name = "updatedItemTopic")
    public NewTopic updateItemTopic(){
        return TopicBuilder
                .name(TOPIC_UPDATE_ITEM)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean(name = "soldItemsRequest")
    public NewTopic itemSold(){
        return TopicBuilder
                .name(TOPIC_REQUEST_FOR_DELETION)
                .partitions(1)
                .replicas(1)
                .build();
    }

}
