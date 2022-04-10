package com.marketplace.itemstorageservice.services;

import com.marketplace.itemstorageservice.DTOmodels.ItemDetailedInfoDTO;
import com.marketplace.itemstorageservice.configs.KafkaContainerConfig;
import com.marketplace.itemstorageservice.configs.ServiceTestConfig;
import com.marketplace.itemstorageservice.exceptions.CustomItemsException;
import com.marketplace.itemstorageservice.models.Item;
import com.marketplace.itemstorageservice.repositories.BrandNameRepo;
import com.marketplace.itemstorageservice.repositories.ItemRepo;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.marketplace.itemstorageservice.utilFunctions.HelpTestFunctions.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("service-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = ServiceTestConfig.Initializer.class, classes = {ServiceTestConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {"/db/changelog/changeSetTest/insert-into-testTables.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"/db/changelog/changeSetTest/remove-after-test.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ItemServiceDeleteItemTest {

    private static final String REDIS_KEY = "itemstorage";
    public static KafkaMessageListenerContainer<String, List<ItemDetailedInfoDTO>> listenerContainer;
    public static ConcurrentLinkedQueue<ConsumerRecord<String, List<ItemDetailedInfoDTO>>> records = new ConcurrentLinkedQueue<>();

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

    @BeforeEach
    void init(){
        listenerContainer = KafkaContainerConfig.getContainer().getMessageContainer(updateItemTopic);
        listenerContainerSetup(records,listenerContainer);
    }

    @AfterEach
    void destroy(){
        if (listenerContainer!=null){
            Runtime.getRuntime().addShutdownHook(new Thread(()->listenerContainer.stop()));
        }
    }

    @Transactional
    @DisplayName("""
            delete item and all related children recursively (cascade ALL)
            also if parent has only deleted item, it also will be removed from database as empty package
            """)
    @ParameterizedTest(name = "test case: => itemId={0}")
    @ValueSource(longs = {1,4,9,100})
    void deleteItemTest(long itemId) throws InterruptedException {
        //given -> fetch item from database
        HashOperations<String, String, Item> itemsCache = itemsCacheTemplate.opsForHash();
        Item item = itemRepo.findById(itemId).orElse(null);
        //when
        if (item == null){
            /*
            when item is null then we simply throw custom exception
            */
            itemServiceMocked();
            doThrow(CustomItemsException.class).when(itemService).itemDeleted(itemId);
            InOrder order = Mockito.inOrder(itemRepo, itemService);
            order.verify(itemRepo, never()).save(Mockito.any(Item.class));
            order.verify(itemService, never()).sendRequestForPackageUpdate(item);
        }
        else {
            /*
            when item not null:
            -> then remove from database and cache
            -> check if item all item,s children also are removed as well
            */
            List<Long> itemChildren = item.getChildItems().stream().map(Item::getId).toList();
            Item parent = item.getParentItem();
            long parentId = parent!=null ? parent.getId() : -1L;
            int parentChildrenBefore = parent!=null ? parent.getChildItems().size() : 0;

            String answer = itemService.itemDeleted(itemId);
            Map<Long, ItemDetailedInfoDTO> packages = fetchPackagesFromKafka(records);

            Item fromCache = itemsCache.get(REDIS_KEY, String.valueOf(itemId));
            checkIfAllChildrenAreRemoved(itemChildren, itemRepo, itemsCache, REDIS_KEY);
            assertThat(answer).contains(String.valueOf(itemId));
            assertNull(fromCache);
            //then check if item has parent
            if (parent!=null){
                //then -> if parent not null get children count BEFORE removal of the item
                if (parentChildrenBefore>1){
                    /*
                    -> if children more than 1:
                        -> fetch parent from database and get updated children count
                        -> children count AFTER item removal must be decreased by 1
                        -> updated package have been send to broker
                     */
                    parent = itemRepo.getById(parentId);
                    int parentChildrenAfter = parent.getChildItems().size();
                    assertEquals(parentChildrenAfter, parentChildrenBefore-1);
                    assertTrue(packages.get(parentId).getItemsQuantityInPack()>0);
                }
                else {
                    /*
                    -> if children count BEFORE was 1:
                        -> parent must be removed from database and cache as empty package
                        -> send to broker as empty package, in order to inform another service that particular package is empty
                     */
                    parent = itemRepo.findById(parentId).orElse(null);
                    fromCache = itemsCache.get(REDIS_KEY, String.valueOf(parentId));
                    assertNull(parent);
                    assertNull(fromCache);
                    assertEquals(0, packages.get(parentId).getItemsQuantityInPack());
                }
            }
            else {
                // if parent is null -> then no call to broker
                assertTrue(packages.isEmpty());
            }
        }
    }

    private void itemServiceMocked(){
        itemRepo = mock(ItemRepo.class);
        brandNameRepo = mock(BrandNameRepo.class);
        itemService = mock(ItemService.class);
    }

}
