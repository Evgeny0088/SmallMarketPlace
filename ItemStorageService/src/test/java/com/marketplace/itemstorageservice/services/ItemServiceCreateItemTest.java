package com.marketplace.itemstorageservice.services;

import com.marketplace.itemstorageservice.configs.ServiceTestConfig;
import com.marketplace.itemstorageservice.models.BrandName;
import com.marketplace.itemstorageservice.models.Item;
import com.marketplace.itemstorageservice.models.ItemType;
import com.marketplace.itemstorageservice.repositories.BrandNameRepo;
import com.marketplace.itemstorageservice.repositories.ItemRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import java.util.stream.IntStream;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = ServiceTestConfig.Initializer.class, classes = {ServiceTestConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {"/db/changelog/changeSetTest/insert-into-BrandTable.sql"})
class ItemServiceCreateItemTest {

    @Autowired
    ItemService itemService;
    @Autowired
    ItemRepo itemRepo;
    @Autowired
    BrandService brandService;
    @Autowired
    BrandNameRepo brandNameRepo;

    @Test
    @DisplayName("get all items")
    void allItemsTest() {
        itemsPersist();
    }

    @Test
    @DisplayName("create new item when parent is null")
    void createNewItemTest() {

    }

    private void itemsPersist(){
        int[] range = new int[]{0,1,2};
        BrandName brand = brandNameRepo.findById(1L).orElse(null);
        IntStream.of(range).forEach(i->{
            Item item = new Item(100L, brand,null, ItemType.PACK);
            item.setId((long)i);
            itemService.createNewItem(item);
        });
    }
}