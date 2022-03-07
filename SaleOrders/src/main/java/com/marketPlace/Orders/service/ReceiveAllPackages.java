package com.marketPlace.Orders.service;

import com.marketPlace.Orders.DTOModels.ItemDetailedInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("receiveAllPackages")
@Slf4j
public class ReceiveAllPackages {

    private final ItemConsumerService<ItemDetailedInfoDTO> itemConsumerService;

    @Autowired
    public ReceiveAllPackages(ItemConsumerService<ItemDetailedInfoDTO> itemConsumerService) {
        this.itemConsumerService = itemConsumerService;
    }

    public void receiveAllPackagesFromItemStorage(){
        List<ItemDetailedInfoDTO> itemList = new ArrayList<>();
        itemConsumerService.receiveAllPackagesFromItemStorage(itemList);
    }
}
