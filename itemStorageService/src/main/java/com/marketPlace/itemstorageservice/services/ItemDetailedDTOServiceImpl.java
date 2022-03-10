package com.marketPlace.itemstorageservice.services;

import com.marketPlace.itemstorageservice.DTOmodels.ItemDetailedInfoDTO;
import com.marketPlace.itemstorageservice.DTOmodels.ItemSoldDTO;
import com.marketPlace.itemstorageservice.repositories.ItemRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ItemDetailedDTOServiceImpl implements ItemDetailedDTOService<ItemDetailedInfoDTO,ItemSoldDTO> {

    private final ItemRepo itemRepo;
    private Optional<ItemDetailedInfoDTO> updatedItem;

    @Autowired
    public ItemDetailedDTOServiceImpl(ItemRepo itemRepo) {
        this.itemRepo = itemRepo;
        this.updatedItem = Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDetailedInfoDTO> allItems() {
        return itemRepo.findAllItemsDTO();
    }

    @Override
    public ItemDetailedInfoDTO getUpdatedItemDetailedDTO(long package_id) {
        return itemRepo.getItemDTOByParentId(package_id);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = RuntimeException.class)
    public int removeItemsFromPackage(ItemSoldDTO itemSoldDTO) {
        int removedRows = itemRepo.removeItemsFromPackage(itemSoldDTO.packageID(),(long)itemSoldDTO.itemSoldQuantity());
        if (removedRows >0 ){
            log.info("{} sold items are removed successfully from database!...",itemSoldDTO.itemSoldQuantity());
        }
        else {
            log.warn("failed to remove {} items, not enough items in database, please reduce sold items count!...",itemSoldDTO.itemSoldQuantity());
        }
        return removedRows;
    }

    @KafkaListener(topics = {"${itemDTO.topic.name_3}"}, containerFactory = "itemSoldConsumerFactory")
    public Optional<ItemDetailedInfoDTO> requestForRemovalOfSoldItems(ItemSoldDTO request) {
        if (request != null){
            log.info("request from SaleOrders server to remove {} sold items from package {} ...",
                    request.itemSoldQuantity(),request.packageID());
            removeItemsFromPackage(request);
            return updatedItem = Optional.ofNullable(itemRepo.getItemDTOByParentId(request.packageID()));
        }else {
            log.error("wrong request from SaleOrders service, check inputs");
            return Optional.empty();
        }
    }
}