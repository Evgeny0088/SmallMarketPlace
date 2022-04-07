package com.marketplace.itemstorageservice.utilFunctions;

import com.marketplace.itemstorageservice.DTOmodels.ItemDetailedInfoDTO;
import com.marketplace.itemstorageservice.repositories.BrandNameRepo;
import com.marketplace.itemstorageservice.repositories.ItemRepo;
import com.marketplace.itemstorageservice.services.ItemService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.eclipse.jetty.util.BlockingArrayQueue;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;

public class HelpTestFunctions {

    public static void itemServiceMocked(ItemService itemService, ItemRepo itemRepo, BrandNameRepo brandNameRepo){
        itemRepo = mock(ItemRepo.class);
        brandNameRepo = mock(BrandNameRepo.class);
        itemService = mock(ItemService.class);
    }

    public static BlockingQueue<ConsumerRecord<String, List<ItemDetailedInfoDTO>>> records = new BlockingArrayQueue<>();

    public static Map<Long, ItemDetailedInfoDTO> fetchPackagesFromKafka(
                BlockingQueue<ConsumerRecord<String, List<ItemDetailedInfoDTO>>> records) throws InterruptedException {
        boolean flag = true;
        Map<Long, ItemDetailedInfoDTO> packagesInKafka = new HashMap<>();
        while (flag){
            ConsumerRecord<String, List<ItemDetailedInfoDTO>> rc = records.poll(1, TimeUnit.SECONDS);
            if (rc!=null)
                rc.value().forEach(pack-> packagesInKafka.put(pack.getItemPackageId(), pack));
            if (records.isEmpty())flag = false;
        }
        return packagesInKafka;
    }

    public static void deleteItemsFromDB(EntityManagerFactory emf){
        EntityManager manager = emf.createEntityManager();
        EntityTransaction transaction = manager.getTransaction();
        transaction.begin();
        manager.createQuery("delete from Item").executeUpdate();
        transaction.commit();
    }

}
