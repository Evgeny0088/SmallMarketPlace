package com.marketplace.itemstorageservice.services;

import com.marketplace.itemstorageservice.DTOmodels.ItemDetailedInfoDTO;
import com.marketplace.itemstorageservice.configs.ServiceTestConfig;
import com.marketplace.itemstorageservice.models.Item;
import com.marketplace.itemstorageservice.repositories.BrandNameRepo;
import com.marketplace.itemstorageservice.repositories.ItemRepo;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = ServiceTestConfig.Initializer.class, classes = {ServiceTestConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {"/db/changelog/changeSetTest/insert-into-BrandTable.sql"})
public class KafkaProducerItemServiceTest {

    private static final String REDIS_KEY = "itemstorage";

    @Autowired
    ItemService itemService;

    @Autowired
    ItemRepo itemRepo;

    @Autowired
    BrandNameRepo brandNameRepo;

    @Autowired
    @Qualifier("ItemCacheTemplate")
    RedisTemplate<String, Item> itemsCacheTemplate;

    @Autowired
    @Qualifier("updateItemRequest")
    NewTopic updateItemTopic;

    @Autowired
    @Qualifier("ItemDetailedDTOUpdateProducer")
    KafkaTemplate<String, List<ItemDetailedInfoDTO>> kafkaTemplate;

    @Test
    @DisplayName("send updated package when new Item is created")
    void sendUpdatedPackageOnItemCreation(){


    }

}
