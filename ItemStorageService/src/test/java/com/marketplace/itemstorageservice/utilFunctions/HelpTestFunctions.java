package com.marketplace.itemstorageservice.utilFunctions;

import com.marketplace.itemstorageservice.DTOmodels.ItemDetailedInfoDTO;
import com.marketplace.itemstorageservice.models.BrandName;
import com.marketplace.itemstorageservice.models.Item;
import com.marketplace.itemstorageservice.repositories.ItemRepo;
import com.marketplace.itemstorageservice.services.BrandService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertNull;

public class HelpTestFunctions {

    public static void brandsPersist(BrandService brandService){
        int[] range = new int[]{0,1,2};
        IntStream.of(range).forEach(i->brandService.postNewBrandName(new BrandName(String.format("brand%s",i),"100")));
    }

    public static void listenerContainerSetup(ConcurrentLinkedQueue<ConsumerRecord<String, List<ItemDetailedInfoDTO>>> records,
                                              KafkaMessageListenerContainer<String, List<ItemDetailedInfoDTO>> listenerContainer){
        listenerContainer.setupMessageListener((MessageListener<String, List<ItemDetailedInfoDTO>>)records::add);
        listenerContainer.start();
    }

    public static Map<Long, ItemDetailedInfoDTO> fetchPackagesFromKafka(
            ConcurrentLinkedQueue<ConsumerRecord<String, List<ItemDetailedInfoDTO>>> records) throws InterruptedException {
        boolean flag = true;
        Map<Long, ItemDetailedInfoDTO> packagesInKafka = new HashMap<>();
        // sleep a bit, waiting for kafka consumer receive all messages in topic
        Thread.sleep(2000);
        while (flag){
            ConsumerRecord<String, List<ItemDetailedInfoDTO>> rc = records.poll();
            if (rc!=null)
                rc.value().forEach(pack-> packagesInKafka.put(pack.getItemPackageId(), pack));
            if (records.isEmpty())flag = false;
        }
        return packagesInKafka;
    }

    public static void checkIfAllChildrenAreRemoved(List<Long> childrenIdList,
                                                    ItemRepo itemRepo,
                                                    HashOperations<String, String, Item> itemsCache,
                                                    String cacheKey){
        childrenIdList.forEach(i->{
            Item itemDB = itemRepo.findById(i).orElse(null);
            Item childFromCache = itemsCache.get(cacheKey, String.valueOf(i));
            assertNull(itemDB);
            assertNull(childFromCache);
        });
    }
}
