package com.marketPlace.itemstorageservice.services;

import com.marketPlace.itemstorageservice.DTOModels.ItemDetailedInfoDTO;
import com.marketPlace.itemstorageservice.repositories.ItemRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ItemDetailedDTOService implements ItemDetailedDTOServiceInterface {

    @Autowired
    ItemRepo itemRepo;

    @Override
    public List<ItemDetailedInfoDTO> allItems() {
        return itemRepo.findAllItemsDTO();
    }

    @Override
    public ItemDetailedInfoDTO getItemDTOByParentId(Long parent_id){
        return itemRepo.getItemDTOByParentId(parent_id);
    }
}
