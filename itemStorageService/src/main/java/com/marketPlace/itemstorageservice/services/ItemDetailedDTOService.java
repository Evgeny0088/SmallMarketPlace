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
public class ItemDetailedDTOService implements ItemDetailedDTOServiceInterface<ItemDetailedInfoDTO,ItemSoldDTO>{

    private final ItemRepo itemRepo;
    private Optional<ItemDetailedInfoDTO> updatedItem;

    @Autowired
    public ItemDetailedDTOService(ItemRepo itemRepo) {
        this.itemRepo = itemRepo;
        this.updatedItem = Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDetailedInfoDTO> allItems() {
        return itemRepo.findAllItemsDTO();
    }

    @Override
    public Optional<ItemDetailedInfoDTO> getUpdatedItemDetailedDTO() {
        return updatedItem;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = RuntimeException.class)
    public void removeItemsFromPackage(ItemSoldDTO itemSoldDTO) {
        itemRepo.removeItemsFromPackage(itemSoldDTO.packageID(),(long)itemSoldDTO.itemSoldQuantity());
    }

    @KafkaListener(topics = {"${itemDTO.topic.name_3}"}, containerFactory = "itemSoldConsumerFactory")
    public Optional<ItemDetailedInfoDTO> requestForItemDetailedDTO(ItemSoldDTO request) {
        if (request != null){
            log.info("request from sale order server to remove {} items from package {} ...",
                    request.itemSoldQuantity(),request.packageID());
            removeItemsFromPackage(request);
            return updatedItem = Optional.ofNullable(itemRepo.getItemDTOByParentId(request.packageID()));
        }else {
            log.error("wrong request from sale order service, check inputs");
            return Optional.empty();
        }
    }
}
