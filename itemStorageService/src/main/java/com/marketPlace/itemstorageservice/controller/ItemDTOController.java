package com.marketPlace.itemstorageservice.controller;

import com.marketPlace.itemstorageservice.DTOModels.ItemDetailedInfoDTO;
import com.marketPlace.itemstorageservice.services.ItemDetailedDTOService;
import com.marketPlace.itemstorageservice.services.ItemService;
import com.marketPlace.itemstorageservice.services.KafkaProducerService;
import com.marketPlace.itemstorageservice.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
public class ItemDTOController {

    @Autowired
    ItemService itemService;

    @Autowired
    ItemDetailedDTOService itemDetailedService;

    @Autowired
    KafkaProducerService kafkaProducerService;

    @GetMapping("/itemsDTO")
    public ResponseEntity<List<ItemDetailedInfoDTO>> allDTOItems(){
        HttpHeaders headers = Utils.httpHeader("list of all itemsDTO","");
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(itemDetailedService.allItems());
    }

    @GetMapping("/itemsDTO/{id}")
    public ResponseEntity<ItemDetailedInfoDTO> getItemDTO(@PathVariable("id") Long id){
        ItemDetailedInfoDTO itemDTO = itemDetailedService.getItemDTOByParentId(id);
        kafkaProducerService.sendItemDTO(itemDTO);
        HttpHeaders headers = Utils.httpHeader("itemDTO","");
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(itemDTO);
    }
}