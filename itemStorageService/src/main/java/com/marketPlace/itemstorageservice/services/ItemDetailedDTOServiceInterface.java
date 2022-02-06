package com.marketPlace.itemstorageservice.services;

import com.marketPlace.itemstorageservice.DTOModels.ItemDetailedInfoDTO;

import java.util.List;

public interface ItemDetailedDTOServiceInterface {

    List<ItemDetailedInfoDTO> allItems();
    ItemDetailedInfoDTO getItemDTOByParentId(Long parent_id);
}
