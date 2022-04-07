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
import com.marketplace.itemstorageservice.utilFunctions.ItemUpdateInValidArguments;
import com.marketplace.itemstorageservice.utilFunctions.ItemUpdateValidArguments;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
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
import org.springframework.kafka.listener.MessageListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.marketplace.itemstorageservice.utilFunctions.HelpTestFunctions.fetchPackagesFromKafka;
import static com.marketplace.itemstorageservice.utilFunctions.HelpTestFunctions.itemServiceMocked;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = ServiceTestConfig.Initializer.class, classes = {ServiceTestConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {"/db/changelog/changeSetTest/insert-into-BrandTable.sql"})
class ItemServiceUpdateItemTest {

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

    public static KafkaMessageListenerContainer<String, List<ItemDetailedInfoDTO>> listenerContainer = KafkaContainerConfig.getContainer().getMessageContainer();

    public static BlockingQueue<ConsumerRecord<String, List<ItemDetailedInfoDTO>>> records = new LinkedBlockingQueue<>();

    @BeforeAll
    static void init(){
        listenerContainer.setupMessageListener((MessageListener<String, List<ItemDetailedInfoDTO>>)records::add);
        listenerContainer.start();
    }

    @AfterAll
    static void destroy(){
        listenerContainer.stop();
    }

    @DisplayName("update new item with valid inputs")
    @ParameterizedTest(name = "test case: => itemId={0}, serial={1}, brandName={2}, parentId={3}, type={4}")
    @ArgumentsSource(ItemUpdateValidArguments.class)
    void updateItemTest(long itemId, long serial, String brandName, long parentId, ItemType type) throws InterruptedException {
        //given
        HashOperations<String, String, Item> itemsCache = itemsCacheTemplate.opsForHash();
        BrandName brand = brandNameRepo.findByName(brandName);
        Item itemFromDB = itemRepo.findById(itemId).orElse(null);
        Item parentFromDB = itemFromDB!=null ? itemFromDB.getParentItem() : null;

        Item parent = itemRepo.findById(parentId).orElse(null);
        Item item = new Item(serial, brand, parent, type);

        // check children count for parent in inputs and from database before item update
        int childrenBeforeParentDB = parentFromDB != null ? parentFromDB.getChildItems().size() : 0;
        int childrenBefore = parent != null ? parent.getChildItems().size() : 0;

        //when -> update we check if any package was stored into kafka
        // it can be two times per updateItem method:
        // when parent !=null AND parentDB!=null as well, that is why we put result into Map
        itemService.updateItem(itemId, item);
        Map<Long, ItemDetailedInfoDTO> packages = fetchPackagesFromKafka(records);

        int childrenAfterParentDB;
        int childrenAfter;

        parentFromDB = parentFromDB!=null ? itemRepo.findById(parentFromDB.getId()).orElse(null) : null;
        parent = itemRepo.findById(parentId).orElse(null);
        //check children count after updating
        //then -> if parent is exist for new item then children count must be incremented on 1,
        if (parent==null && parentFromDB!=null){
            childrenAfterParentDB = parentFromDB.getChildItems().size();
            Item parentDBFromCache = itemsCache.get(REDIS_KEY, String.valueOf(parentFromDB.getId()));
            assertNotNull(packages.get(parentFromDB.getId()));
            assertNotNull(parentDBFromCache);
            assertThat(childrenAfterParentDB).isEqualTo(childrenBeforeParentDB-1);
        }
        else if (parent!=null){
            childrenAfter = parent.getChildItems().size();
            Item parentFromCache = itemsCache.get(REDIS_KEY, String.valueOf(parentId));
            assertNotNull(packages.get(parentId));
            assertNotNull(parentFromCache);
            if (parentFromDB != null && !parent.getId().equals(parentFromDB.getId())){
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
            assertNotNull(ItemFromCache);
        }
    }

    @DisplayName("update new item with valid inputs")
    @ParameterizedTest(name = "test case: => itemId={0}, serial={1}, brandName={2}, parentId={3}, type={4}")
    @ArgumentsSource(ItemUpdateInValidArguments.class)
    void updateFailedItemTest(long itemId, long serial, String brandName, long parentId, ItemType type) {
        //given -> mocked object to invoke exceptions on test cases
        BrandName brand = brandNameRepo.findByName(brandName);
        Item parent = itemRepo.findById(parentId).orElse(null);
        Item item = new Item(serial, brand, parent, type);
        itemServiceMocked(itemService, itemRepo, brandNameRepo);

        //when and then
        doThrow(CustomItemsException.class).when(itemService).updateItem(itemId, item);
        InOrder order = Mockito.inOrder(itemRepo, itemService);
        order.verify(itemRepo, never()).save(any(Item.class));
        order.verify(itemService, never()).sendRequestForPackageUpdate(item);
    }
}