package com.marketplace.orders.service;

import com.marketplace.orders.DTOModels.ItemDetailedInfoDTO;
import com.marketplace.orders.DTOModels.ItemSoldDTO;
import com.marketplace.orders.exceptions.CustomItemsException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.ehcache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
@Slf4j
public class ItemProducerServiceImpl implements ItemProducerService {

    private final NewTopic itemSold;
    private final KafkaTemplate<Long, ItemSoldDTO> itemSoldTemplate;
    private final Cache<Long, ItemDetailedInfoDTO> itemDetailedInfoDTOCache;

    @Autowired
    public ItemProducerServiceImpl(NewTopic itemSold, KafkaTemplate<Long, ItemSoldDTO> itemSoldTemplate, Cache<Long, ItemDetailedInfoDTO> itemDetailedInfoDTOCache) {
        this.itemSold = itemSold;
        this.itemSoldTemplate = itemSoldTemplate;
        this.itemDetailedInfoDTOCache = itemDetailedInfoDTOCache;
    }

    @Override
    public boolean sendSoldItem(long id, int quantity) {
        if (itemDetailedInfoDTOCache.containsKey(id)){
            ItemDetailedInfoDTO packageOfSoldItems = itemDetailedInfoDTOCache.get(id);
            long currentQuantity = packageOfSoldItems.getItemsQuantityInPack()-quantity;
            if (currentQuantity<0){
                log.warn("not enough items in current package: <{}>, need to check in DB...",currentQuantity);
                return false;
            }else {
                packageOfSoldItems.setItemsQuantityInPack(currentQuantity);
                ItemSoldDTO soldItems = new ItemSoldDTO(id,quantity);
                ListenableFuture<SendResult<Long, ItemSoldDTO>> future = itemSoldTemplate.send(itemSold.name(), soldItems);
                future.addCallback(new ListenableFutureCallback<>(){
                    @Override
                    public void onSuccess(SendResult<Long, ItemSoldDTO> result) {
                        log.info("package with id <{}> and sold items quantity <{}> sent successfully!",id,quantity);
                    }
                    @Override
                    public void onFailure(Throwable ex) {
                        log.info(String.format("message failed to send, see stack trace below:\n%s", ex.getMessage()));
                    }
                });
            }
        }else {
            log.error("package with id <{}> does not available in SaleOrders service >>> ...", id);
            throw new CustomItemsException(id, "package not found!...");
        }
        return true;
    }
}
