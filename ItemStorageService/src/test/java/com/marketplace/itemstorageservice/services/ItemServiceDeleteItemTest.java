package com.marketplace.itemstorageservice.services;

import com.marketplace.itemstorageservice.DTOmodels.ItemDetailedInfoDTO;
import com.marketplace.itemstorageservice.configs.ServiceTestConfig;
import com.marketplace.itemstorageservice.exceptions.CustomItemsException;
import com.marketplace.itemstorageservice.models.Item;
import com.marketplace.itemstorageservice.repositories.BrandNameRepo;
import com.marketplace.itemstorageservice.repositories.ItemRepo;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = ServiceTestConfig.Initializer.class, classes = {ServiceTestConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {"/db/changelog/changeSetTest/insert-into-BrandTable.sql"})
public class ItemServiceDeleteItemTest {

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
    KafkaTemplate<String, List<ItemDetailedInfoDTO>> kafkaTemplateMock;

    @DisplayName("delete item and all related children recursively")
    @ParameterizedTest(name = "test case: => itemId={0}")
    @ValueSource(longs = {1,4,9,100})
    void deleteItemTest(long itemId){
        //given
        Item item = itemRepo.findById(itemId).orElse(null);
        //when
        if (item == null){
            itemServiceMocked();
            //then
            doThrow(CustomItemsException.class).when(itemService).itemDeleted(itemId);
            InOrder order = Mockito.inOrder(itemRepo, itemService);
            order.verify(itemRepo, never()).save(Mockito.any(Item.class));
            order.verify(itemService, never()).sendRequestForPackageUpdate(item);
        } else {
            Item parent = item.getParentItem();
            int childrenCount = parent!=null ? parent.getChildItems().size() : 0;
            long parentId = parent!=null ? parent.getId() : -1L;
            //then -> get parent of deleted item
            //then-> check if item deleted and message is send
            String answer = itemService.itemDeleted(itemId);
            assertThat(answer).contains(String.valueOf(itemId));
            // then -> check if it is removed from redis cache
            HashOperations<String, String, Item> itemsCache = itemsCacheTemplate.opsForHash();
            Item fromCache = itemsCache.get(REDIS_KEY, String.valueOf(itemId));
            assertNull(fromCache);
            //then -> check if all children is removed if exists from DB and redis cache
            List<Long> children = item.getChildItems().stream().map(Item::getId).toList();
            children.forEach(i->{
                Item itemDB = itemRepo.findById(i).orElse(null);
                Item childFromCache = itemsCache.get(REDIS_KEY, String.valueOf(i));
                assertNull(itemDB);
                assertNull(childFromCache);
            });
            //then if parent not null and do not have only deleted item, then it also must be deleted
            if (parent!=null && childrenCount==1){
                parent = itemRepo.findById(parentId).orElse(null);
                //try to fetch parent from DB, and it must be null
                fromCache = itemsCache.get(REDIS_KEY, String.valueOf(parentId));
                assertNull(parent);
                assertNull(fromCache);
            }
        }
    }

    private void itemServiceMocked(){
        itemRepo = mock(ItemRepo.class);
        brandNameRepo = mock(BrandNameRepo.class);
        itemService = mock(ItemService.class);
    }

}
