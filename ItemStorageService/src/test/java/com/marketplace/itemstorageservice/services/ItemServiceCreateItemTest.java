package com.marketplace.itemstorageservice.services;

import com.marketplace.itemstorageservice.DTOmodels.ItemDetailedInfoDTO;
import com.marketplace.itemstorageservice.configs.ServiceTestConfig;
import com.marketplace.itemstorageservice.exceptions.CustomItemsException;
import com.marketplace.itemstorageservice.models.BrandName;
import com.marketplace.itemstorageservice.models.Item;
import com.marketplace.itemstorageservice.models.ItemType;
import com.marketplace.itemstorageservice.repositories.BrandNameRepo;
import com.marketplace.itemstorageservice.repositories.ItemRepo;
import com.marketplace.itemstorageservice.utilFunctions.ItemCreationInValidArguments;
import com.marketplace.itemstorageservice.utilFunctions.ItemCreationValidArguments;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = ServiceTestConfig.Initializer.class, classes = {ServiceTestConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {"/db/changelog/changeSetTest/insert-into-BrandTable.sql"})
class ItemServiceCreateItemTest {

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

    @Order(1)
    @Test
    @DisplayName("get all items")
    void allItemsTest() {
        HashOperations<String, String, Item> itemsCache = itemsCacheTemplate.opsForHash();
        List<Item> items = itemService.allItems();
        assertThat(items.size()).isEqualTo(itemsCache.entries(REDIS_KEY).size());
    }

    @Order(2)
    @DisplayName("create new item with valid inputs")
    @ParameterizedTest(name = "test case: => serial={0}, brandName={1}, parentId={2}, type={3}")
    @ArgumentsSource(ItemCreationValidArguments.class)
    void createNewItemTest(long serial, String brandName, long parentId, ItemType type) {
        //given
        HashOperations<String, String, Item> itemsCache = itemsCacheTemplate.opsForHash();
        List<Item> itemsBefore = itemService.allItems();
        BrandName brand = brandNameRepo.findByName(brandName);
        Item parent = itemRepo.findById(parentId).orElse(null);
        int childrenBefore = parent != null ? parent.getChildItems().size() : 0;
        Item item = new Item(serial, brand, parent, type);
        //when
        itemService.createNewItem(item);
        List<Item> itemsAfter = itemService.allItems();
        parent = itemRepo.findById(parentId).orElse(null);
        int childrenAfter = parent != null ? parent.getChildItems().size() : 0;
        //then
        assertThat(itemsAfter.size()).isEqualTo(itemsBefore.size()+1);
        //then -> if parent is exist for new item then children count must be incremented on 1,
        if (parent!=null){
            Item fromCache = itemsCache.get(REDIS_KEY, String.valueOf(parentId));
            assertNotNull(fromCache);
            assertThat(childrenAfter).isEqualTo(childrenBefore+1);
        }else {
            Item fromCacheItem = itemsCache.get(REDIS_KEY, String.valueOf(item.getId()));
            assertNotNull(fromCacheItem);
        }
    }

    @Order(3)
    @DisplayName("create new item failed with invalid inputs")
    @ParameterizedTest(name = "test case: => serial={0}, brandName={1}, parentId={2}, type={3}")
    @ArgumentsSource(ItemCreationInValidArguments.class)
    void createNewItemFailedTest(long serial, String brandName, long parentId, ItemType type) {
        //given -> mocked object to invoke exceptions on test cases
        BrandName brand = brandNameRepo.findByName(brandName);
        Item parent = itemRepo.findById(parentId).orElse(null);
        Item item = new Item(serial, brand, parent, type);
        itemServiceMocked();
        //when and then
        doThrow(CustomItemsException.class).when(itemService).createNewItem(item);
        InOrder order = Mockito.inOrder(itemRepo, itemService);
        order.verify(itemRepo, never()).save(Mockito.any(Item.class));
        order.verify(itemService, never()).sendRequestForPackageUpdate(item);
    }

    private void itemServiceMocked(){
        itemRepo = mock(ItemRepo.class);
        brandNameRepo = mock(BrandNameRepo.class);
        itemService = mock(ItemService.class);
    }
}