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
import com.marketplace.itemstorageservice.utilFunctions.ItemCreationInvalidArguments;
import com.marketplace.itemstorageservice.utilFunctions.ItemCreationValidArguments;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.marketplace.itemstorageservice.utilFunctions.HelpTestFunctions.fetchPackagesFromKafka;
import static com.marketplace.itemstorageservice.utilFunctions.HelpTestFunctions.listenerContainerSetup;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("service-test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = ServiceTestConfig.Initializer.class, classes = {ServiceTestConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {"/db/changelog/changeSetTest/insert-into-testTables.sql"})
class ItemServiceCreateItemTest {

    private static final String REDIS_KEY = "itemstorage";
    public static ConcurrentLinkedQueue<ConsumerRecord<String, List<ItemDetailedInfoDTO>>> records = new ConcurrentLinkedQueue<>();
    public static KafkaMessageListenerContainer<String, List<ItemDetailedInfoDTO>> listenerContainer;

    @Autowired
    LoadAllPackages loadAllPackages;

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
    @Qualifier("allPackagesTopic")
    NewTopic allPackagesTopic;

    @AfterEach
    void destroy(){
        if (listenerContainer!=null){
            Runtime.getRuntime().addShutdownHook(new Thread(()->listenerContainer.stop()));
        }
    }

    @Order(1)
    @Test
    @DisplayName("get all items")
    void allItemsTest() {
        HashOperations<String, String, Item> itemsCache = itemsCacheTemplate.opsForHash();
        List<Item> items = itemService.allItems();
        assertThat(items.size()).isEqualTo(itemsCache.entries(REDIS_KEY).size());
    }

    @Order(2)
    @DisplayName("""
                load all packages in kafka broker.
                Purpose of this method is to deliver valid packages (with items) to saleorder service
                on application start up.
                If packages (item with item_type=PACK) has any items, then we send it to another service.
                If it,s empty (no items with item_type=ITEM) then nothing would be send.
                """)
    @ParameterizedTest(name = "test case: => isNothingToSend={0}")
    @ValueSource(booleans = {false, true})
    void loadAllPackagesTest(boolean isNothingToSend) throws InterruptedException {
        //given -> setup kafka listener for packages retrieval from kafka if they have been send
        listenerContainer = KafkaContainerConfig.getContainer().getMessageContainer(allPackagesTopic);
        listenerContainerSetup(records,listenerContainer);
        Map<Long, ItemDetailedInfoDTO> packages;
        //then
        if (!isNothingToSend){ //case when we have something to send
            loadAllPackages.loadAllItemsFromDB();
            packages = fetchPackagesFromKafka(records);
            List<ItemDetailedInfoDTO> packagesDB = itemRepo.findAllItemsDTO();
            assertEquals(packagesDB.size(),packages.values().size());
        }
        //case when packages are empty -> preliminary we remove all items from db, therefore we got no packages
        else {
            itemRepo.deleteAllItems();
            loadAllPackages.loadAllItemsFromDB();
            packages = fetchPackagesFromKafka(records);
            assertTrue(packages.isEmpty());
        }
    }

    @Order(3)
    @DisplayName("""
            create new item with valid inputs.
            As have seen, this method too complex and hard to catch.
            Therefore it is recommended to apply strategy pattern to make it easier readable.
            """)
    @ParameterizedTest(name = "test case: => serial={0}, brandName={1}, parentId={2}, type={3}")
    @ArgumentsSource(ItemCreationValidArguments.class)
    void createNewItemTest(long serial, String brandName, long parentId, ItemType type) throws InterruptedException {
        /*
        given:
        -> get list of items BEFORE saving new item
        -> prepare new Item for saving in database
        -> get count of children items inside parent item (item_type=PACK) if exists
         */
        List<Item> itemsBefore = itemService.allItems();
        BrandName brand = brandNameRepo.findByName(brandName);
        Item parent = itemRepo.findById(parentId).orElse(null);
        Item item = new Item(serial, brand, parent, type);
        int childrenBefore = parent != null ? parent.getChildItems().size() : 0;

        //given -> setup kafka listener for packages retrieval from kafka if they have been send
        //given -> prepare redis cache hashset client
        HashOperations<String, String, Item> itemsCache = itemsCacheTemplate.opsForHash();
        listenerContainer = KafkaContainerConfig.getContainer().getMessageContainer(updateItemTopic);
        listenerContainerSetup(records,listenerContainer);

        /*
        when:
         -> create Item and retrieve it from database
         -> get parent item if exists
         -> get children count AFTER saving (it must be incremented by 1)
         */
        itemService.createNewItem(item);
        parent = itemRepo.findById(parentId).orElse(null);
        List<Item> itemsAfter = itemService.allItems();
        int childrenAfter = parent != null ? parent.getChildItems().size() : 0;

        //then -> check if item inserted in db and item count in database is incremented by 1
        assertThat(itemsAfter.size()).isEqualTo(itemsBefore.size()+1);

        //then -> fetch elements from broker under specified topic
        Map<Long, ItemDetailedInfoDTO> fetchFromKafka = fetchPackagesFromKafka(records);

        /*
        then:
         -> if parent is exist for new item then children count must be incremented by 1,
         -> check if broker listener has updated package
         -> if parent does not exist then broker is empty
         -> saved item is sent to cache
        */
        if (parent!=null){
            Item fromCache = itemsCache.get(REDIS_KEY, String.valueOf(parentId));
            assertNotNull(fetchFromKafka);
            assertThat(fetchFromKafka.size()).isEqualTo(1);
            assertNotNull(fromCache);
            assertThat(childrenAfter).isEqualTo(childrenBefore+1);
        }else {
            Item fromCacheItem = itemsCache.get(REDIS_KEY, String.valueOf(item.getId()));
            assertTrue(fetchFromKafka.isEmpty());
            assertNotNull(fromCacheItem);
        }
    }

    @Order(4)
    @DisplayName("create new item failed with invalid inputs")
    @ParameterizedTest(name = "test case: => serial={0}, brandName={1}, parentId={2}, type={3}")
    @ArgumentsSource(ItemCreationInvalidArguments.class)
    void createNewItemFailedTest(long serial, String brandName, long parentId, ItemType type) {
        /*
        given:
        -> mocked object to invoke exceptions on test cases
        -> setup listener with specific topic
        */
        BrandName brand = brandNameRepo.findByName(brandName);
        Item parent = itemRepo.findById(parentId).orElse(null);
        Item item = new Item(serial, brand, parent, type);
        itemServiceMocked();
        //when and then
        doThrow(CustomItemsException.class).when(itemService).createNewItem(item);
        InOrder order = Mockito.inOrder(itemRepo, itemService);
        order.verify(itemRepo, never()).save(item);
        order.verify(itemService, never()).sendRequestForPackageUpdate(item);
    }

    private void itemServiceMocked(){
        itemService = mock(ItemService.class);
        itemRepo = mock(ItemRepo.class);
        brandNameRepo = mock(BrandNameRepo.class);
    }
}