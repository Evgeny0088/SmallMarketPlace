package com.marketplace.itemstorageservice.services;

import com.marketplace.itemstorageservice.DTOmodels.ItemDetailedInfoDTO;
import com.marketplace.itemstorageservice.DTOmodels.ItemSoldDTO;
import com.marketplace.itemstorageservice.repositories.ItemRepo;
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
@Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = RuntimeException.class)
public class ItemDetailedDTOServiceImpl implements ItemDetailedDTOService<ItemDetailedInfoDTO,ItemSoldDTO> {

    private final ItemRepo itemRepo;

    @Autowired
    public ItemDetailedDTOServiceImpl(ItemRepo itemRepo) {
        this.itemRepo = itemRepo;
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
    public int removeItemsFromPackage(ItemSoldDTO itemSoldDTO) {
        int removedRows = itemRepo.removeItemsFromPackage(itemSoldDTO.packageID(), itemSoldDTO.itemSoldQuantity());
        if (removedRows >0 ){
            log.info("{} sold items are removed successfully from database!...",itemSoldDTO.itemSoldQuantity());
        }
        else {
            log.warn("failed to remove {} items, not enough items in database or package is not valid!...",itemSoldDTO.itemSoldQuantity());
        }
        return removedRows;
    }

    @KafkaListener(topics = {"${itemDTO.topic.name_3}"}, containerFactory = "itemSoldConsumerFactory")
    public Optional<ItemDetailedInfoDTO> requestForRemovalOfSoldItems(ItemSoldDTO request) {
        if (request != null){
            log.info("request from SaleOrders server to remove {} sold items from package {} ...",
                    request.itemSoldQuantity(),request.packageID());
            int soldItems = removeItemsFromPackage(request);
            ItemDetailedInfoDTO item = itemRepo.getItemDTOByParentId(request.packageID());
            log.info("package:{}",item.toString());
        }else {
            log.error("wrong request from SaleOrders service, check inputs");
        }
        return Optional.empty();
    }
}