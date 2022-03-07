package com.marketPlace.itemstorageservice.services;

import com.marketPlace.itemstorageservice.DTOmodels.ItemDetailedInfoDTO;
import com.marketPlace.itemstorageservice.DTOmodels.ItemSoldDTO;
import com.marketPlace.itemstorageservice.models.BrandName;
import com.marketPlace.itemstorageservice.models.Item;
import com.marketPlace.itemstorageservice.models.ItemType;
import com.marketPlace.itemstorageservice.repositories.BrandNameRepo;
import com.marketPlace.itemstorageservice.repositories.ItemRepo;
import com.marketPlace.itemstorageservice.exceptions.CustomItemsException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.ehcache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ItemService implements ItemServiceInterface{

    private final Cache<Long, Item> itemCache;
    private final ItemRepo itemRepo;
    private final BrandNameRepo brandRepo;
    private final KafkaTemplate<String, ItemDetailedInfoDTO> singleItemDTOProducer;
    private final KafkaTemplate<String, ItemSoldDTO> itemCountReduction;
    private final NewTopic updateItemRequest;

    @Autowired
    public ItemService(Cache<Long, Item> itemCache, ItemRepo itemRepo, BrandNameRepo brandRepo,
                       KafkaTemplate<String, ItemDetailedInfoDTO> singleItemDTOProducer,
                       KafkaTemplate<String, ItemSoldDTO> itemCountReduction,
                       @Qualifier("updateItemRequest") NewTopic updateItemRequest) {
        this.itemCache = itemCache;
        this.itemRepo = itemRepo;
        this.brandRepo = brandRepo;
        this.singleItemDTOProducer = singleItemDTOProducer;
        this.itemCountReduction = itemCountReduction;
        this.updateItemRequest = updateItemRequest;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Item> allItems(){
        List<Item> items = itemRepo.findAll();
        items.forEach(item->itemCache.put(item.getId(),item));
        return items;
    }

    @Override
    @Transactional(rollbackFor = CustomItemsException.class)
    public void createNewItem(Item item) throws CustomItemsException{
        BrandName brand = isValidBrand(item);
        if (brand!=null){
            item.setBrandName(brand);
            item.setCreationDate(LocalDateTime.now());
            Item parent = isNotNullParent(item.getParentItem());
            if (parent!=null) {
                item.setParentItem(parent);
                if (item.getParentItem().getItem_type() == ItemType.ITEM){
                    throw new CustomItemsException(item.getItem_type().name(),
                            "parent should have PACK item type, check inputs!");
                }
                if (item.getParentItem().getBrandName() != item.getBrandName()){
                    throw new CustomItemsException(item.getItem_type().name(),
                            "item and parent should have same brand, check inputs!");
                }
                parent.getChildItems().add(item);
                itemRepo.save(parent);
                sendRequestForPackageUpdate(parent);
            }else {
                if (item.getItem_type() == ItemType.ITEM){
                    throw new CustomItemsException(item.getItem_type().name(),
                            "item must have PACK type if parent is null, check inputs!");
                }
                item.setParentItem(null);
                itemRepo.save(item);
            }
        }else {
            throw new CustomItemsException(item.getItem_type().name(), "item not possible to create, check inputs!");
        }
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = CustomItemsException.class)
    public void updateItem(Long id,Item item) throws CustomItemsException{
        Item itemDB = itemRepo.findById(id).orElse(null);
        BrandName brand = isValidBrand(item);
        Item parent = isNotNullParent(item.getParentItem());
        if (itemDB!=null && brand!=null){
            itemDB.setBrandName(brand);
            itemDB.setSerial(item.getSerial());
            itemDB.setItem_type(item.getItem_type());
            itemDB.setCreationDate(LocalDateTime.now());
            if (parent==null && itemDB.getParentItem()!=null){
                itemDB.getParentItem().getChildItems().remove(itemDB);
                itemDB.setParentItem(null);
                itemRepo.save(itemDB);
                sendRequestForPackageUpdate(itemDB.getParentItem());
            }else {
                if (parent != null){
                    if (parent.getItem_type() == ItemType.ITEM){
                        throw new CustomItemsException(id,
                                "parent should have PACK item type, check inputs!");
                    }
                    if (!parent.getBrandName().getName().equals(itemDB.getBrandName().getName())){
                        throw new CustomItemsException(id,
                                "brand in parent item should be the same as child item, check inputs!");
                    }
                    itemDB.setParentItem(parent);
                    itemRepo.save(itemDB);
                    sendRequestForPackageUpdate(parent);
                }
            }
            itemCache.put(itemDB.getId(), itemDB);
            log.info("itemCache is updated: item <{}>",item.getId());
        }else {
            throw new CustomItemsException(id,"item not possible to update, " + "check inputs!");
        }
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = RuntimeException.class)
    public String itemDeleted(Long id) throws CustomItemsException{
        Item itemToRemove = itemRepo.findById(id).orElse(null);
        if (itemToRemove != null){
            String result = removeSoldItemsFromDB(itemToRemove);
            return String.format(result, id);
        }else {
            throw new CustomItemsException("item with id: " + id, "is not found");
        }
    }

    private String removeSoldItemsFromDB(Item item){
        if (item==null){
            return "item with %d is deleted!";
        }
        Item parent = item.getParentItem();
        Long itemToRemoveId = item.getId();
        itemRepo.deleteById(itemToRemoveId);
        itemCache.remove(itemToRemoveId);
        if (parent != null && parent.getChildItems().isEmpty()){
            sendRequestForPackageUpdate(parent);
            return removeSoldItemsFromDB(parent);
        }else {
            if (parent != null){
                parent.getChildItems().remove(item);
            }
            sendRequestForPackageUpdate(parent);
            return removeSoldItemsFromDB(null);
        }
    }

    private void sendRequestForPackageUpdate(Item parent) {
        long parent_id = parent != null ? parent.getId() : -1L;
        if (parent_id != -1L){
            ItemDetailedInfoDTO updatedPackage = itemRepo.getItemDTOByParentId(parent.getId());
            ListenableFuture<SendResult<String, ItemDetailedInfoDTO>> future = singleItemDTOProducer.send(updateItemRequest.name(),updatedPackage);
            future.addCallback(new ListenableFutureCallback<>(){
                @Override
                public void onSuccess(SendResult<String, ItemDetailedInfoDTO> result) {
                    log.info("package with id <{}> sent successfully!",parent.getId());
                }
                @Override
                public void onFailure(Throwable ex) {
                    log.info(String.format("message failed to send, see stack trace below:\n%s", ex.getMessage()));
                }
            });
        }else {
            ListenableFuture<SendResult<String, ItemSoldDTO>> future = itemCountReduction.send(updateItemRequest.name(), new ItemSoldDTO(parent_id,1));
            future.addCallback(new ListenableFutureCallback<>(){
                @Override
                public void onSuccess(SendResult<String, ItemSoldDTO> result) {
                    log.info("package with id <{}> sent successfully!",parent_id);
                }
                @Override
                public void onFailure(Throwable ex) {
                    log.info(String.format("message failed to send, see stack trace below:\n%s", ex.getMessage()));
                }
            });
        }
        log.info("Updated package is sent to the sale order service");
    }

    private BrandName isValidBrand(Item item) {
        return (item.getBrandName()!=null && brandRepo.findById(item.getBrandName().getId()).isPresent())
                ? brandRepo.getById(item.getBrandName().getId()) : null;
    }

    private Item isNotNullParent(Item parent){
        if (parent==null)return null;
        return itemRepo.findById(parent.getId()).orElse(null);
    }
}
