package com.marketplace.itemstorageservice.services;

import com.marketplace.itemstorageservice.DTOmodels.ItemDetailedInfoDTO;
import com.marketplace.itemstorageservice.configs.KafkaContainerConfig;
import com.marketplace.itemstorageservice.configs.ServiceTestConfig;
import com.marketplace.itemstorageservice.exceptions.CustomItemsException;
import com.marketplace.itemstorageservice.models.BrandName;
import com.marketplace.itemstorageservice.models.Item;
import com.marketplace.itemstorageservice.models.ItemType;
import com.marketplace.itemstorageservice.repositories.BrandNameRepo;
import com.marketplace.itemstorageservice.repositories.ItemRepo;
import com.marketplace.itemstorageservice.utilFunctions.ItemUpdateInvalidArguments;
import com.marketplace.itemstorageservice.utilFunctions.ItemUpdateValidArguments;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
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
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.marketplace.itemstorageservice.utilFunctions.HelpTestFunctions.fetchPackagesFromKafka;
import static com.marketplace.itemstorageservice.utilFunctions.HelpTestFunctions.listenerContainerSetup;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = ServiceTestConfig.Initializer.class, classes = {ServiceTestConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {"/db/changelog/changeSetTest/insert-into-testTables.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"/db/changelog/changeSetTest/remove-after-test.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ItemServiceUpdateItemTest {

    private static final String REDIS_KEY = "itemstorage";
    public static ConcurrentLinkedQueue<ConsumerRecord<String, List<ItemDetailedInfoDTO>>> records = new ConcurrentLinkedQueue<>();
    public static KafkaMessageListenerContainer<String, List<ItemDetailedInfoDTO>> listenerContainer;

    @Autowired
    ItemService itemService;

    @Autowired
    ItemRepo itemRepo;

    @Autowired
    BrandNameRepo brandNameRepo;

    @Autowired
    @Qualifier("updateItemRequest")
    NewTopic updateItemTopic;

    @Autowired
    @Qualifier("ItemCacheTemplate")
    RedisTemplate<String, Item> itemsCacheTemplate;

    @AfterEach
    void destroy(){
        if (listenerContainer!=null){
            Runtime.getRuntime().addShutdownHook(new Thread(()->listenerContainer.stop()));
        }
    }

    @Order(1)
    @DisplayName("update new item with valid inputs")
    @ParameterizedTest(name = "test case: => itemId={0}, serial={1}, brandName={2}, parentId={3}, type={4}")
    @ArgumentsSource(ItemUpdateValidArguments.class)
    void updateItemTest(long itemId, long serial, String brandName, long parentId, ItemType type) throws InterruptedException {
        /*
        given:
        -> setup cache, kafka consumer with specified topic
        -> fetch current item and parent(parentFromDB) from database if exists
        -> check if new parent exists in database
        */
        HashOperations<String, String, Item> itemsCache = itemsCacheTemplate.opsForHash();
        listenerContainer = KafkaContainerConfig.getContainer().getMessageContainer(updateItemTopic);
        listenerContainerSetup(records,listenerContainer);

        BrandName brand = brandNameRepo.findByName(brandName);
        Item itemFromDB = itemRepo.findById(itemId).orElse(null);
        Item parentFromDB = itemFromDB!=null ? itemFromDB.getParentItem() : null;

        Item newParent = itemRepo.findById(parentId).orElse(null);
        Item item = new Item(serial, brand, newParent, type);

        //get children items count for parentFromDB BEFORE update
        //get children items count for updated newParent BEFORE update
        int childrenBeforeParentDB = parentFromDB != null ? parentFromDB.getChildItems().size() : 0;
        int childrenBefore = newParent != null ? newParent.getChildItems().size() : 0;

        /*
        when:
        -> update current item with new inputs
        -> send updated package to broker - there are can be two situations:
            -> send updated package when one of the parents is null (updated newParent or parentFromDB)
            -> send updated packages TWO times when both parents NOT null (two calls to broker)
        */
        itemService.updateItem(itemId, item);
        Map<Long, ItemDetailedInfoDTO> packages = fetchPackagesFromKafka(records);

        //set children counts AFTER update for newParent and parentFromDB
        //fetch parents from database after update
        int childrenAfterParentDB;
        int childrenAfter;
        parentFromDB = parentFromDB!=null ? itemRepo.findById(parentFromDB.getId()).orElse(null) : null;
        newParent = itemRepo.findById(parentId).orElse(null);

        /*
        then:
        -> if newParent exists for updated item then children count must be incremented on 1,
        -> if parentFromDB(former parent) not null then children count must be decreased on 1
        -> in case if parents are the same for updated item OR parents ARE null, then children count remains the same, therefore we DO NOT call to broker
        */
        if (newParent==null && parentFromDB!=null){
            childrenAfterParentDB = parentFromDB.getChildItems().size();
            Item parentDBFromCache = itemsCache.get(REDIS_KEY, String.valueOf(parentFromDB.getId()));
            assertNotNull(packages.get(parentFromDB.getId()));
            assertNotNull(parentDBFromCache);
            assertThat(childrenAfterParentDB).isEqualTo(childrenBeforeParentDB-1);
        }
        else if (newParent!=null){
            childrenAfter = newParent.getChildItems().size();
            Item parentFromCache = itemsCache.get(REDIS_KEY, String.valueOf(parentId));
            assertNotNull(packages.get(parentId));
            assertNotNull(parentFromCache);
            if (parentFromDB != null && !newParent.getId().equals(parentFromDB.getId())){
                Item parentDBFromCache = itemsCache.get(REDIS_KEY, String.valueOf(parentFromDB.getId()));
                childrenAfterParentDB = parentFromDB.getChildItems().size();
                assertNotNull(packages.get(parentFromDB.getId()));
                assertNotNull(parentDBFromCache);
                assertThat(childrenAfter).isEqualTo(childrenBefore+1);
                assertThat(childrenAfterParentDB).isEqualTo(childrenBeforeParentDB-1);
            }
        }
        else {
            Item ItemFromCache = itemsCache.get(REDIS_KEY, String.valueOf(item.getId()));
            assertTrue(packages.isEmpty());
            assertNotNull(ItemFromCache);
        }
    }

    @Order(2)
    @DisplayName("update failed with invalid inputs")
    @ParameterizedTest(name = "test case: => itemId={0}, serial={1}, brandName={2}, parentId={3}, type={4}")
    @ArgumentsSource(ItemUpdateInvalidArguments.class)
    void updateFailedItemTest(long itemId, long serial, String brandName, long parentId, ItemType type) {
        //given -> mocked object to invoke exceptions on test cases
        BrandName brand = brandNameRepo.findByName(brandName);
        Item newParent = itemRepo.findById(parentId).orElse(null);
        Item item = new Item(serial, brand, newParent, type);
        itemServiceMocked();

        //when -> updated method is called it must throw exception with specified inputs
        doThrow(CustomItemsException.class).when(itemService).updateItem(itemId, item);
        //then -> check if updated item was not saved and package wasn,t updated, hence never called to broker
        InOrder order = Mockito.inOrder(itemRepo, itemService);
        order.verify(itemRepo, never()).save(any(Item.class));
        order.verify(itemService, never()).sendRequestForPackageUpdate(item);
    }

    private void itemServiceMocked(){
        itemRepo = mock(ItemRepo.class);
        brandNameRepo = mock(BrandNameRepo.class);
        itemService = mock(ItemService.class);
    }
}