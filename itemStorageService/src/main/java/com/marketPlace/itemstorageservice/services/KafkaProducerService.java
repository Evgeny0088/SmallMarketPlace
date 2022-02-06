package com.marketPlace.itemstorageservice.services;

import com.marketPlace.itemstorageservice.DTOModels.ItemDetailedInfoDTO;

import java.util.List;

public interface KafkaProducerService {

    void sendItemDTO(ItemDetailedInfoDTO itemDTO);
    void sendAllItemsDTOPackages(List<ItemDetailedInfoDTO> itemsDTO);

}
