package com.marketPlace.itemstorageservice.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Slf4j
public class KafkaConsumerConfig {

    private static final String TOPIC_REQUEST_FOR_DELETION = "request_for_deletion";
    private ObjectMapper mapper;

    @Bean
    public NewTopic topicRequestForDeletion() {
        return TopicBuilder
                .name(TOPIC_REQUEST_FOR_DELETION)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @KafkaListener(groupId = "${spring.kafka.consumer.group-id}", topics = TOPIC_REQUEST_FOR_DELETION)
    public void itemDeletionListener(String fromSaleOrder) throws Exception {
        try {
            log.info("message received from saleOrder:{}",fromSaleOrder);
        } catch (Exception ex) {
            log.error("can't parse message:{}", fromSaleOrder, ex);
            throw new Exception("can't parse message:" + ex.getMessage());
        }
    }
}
