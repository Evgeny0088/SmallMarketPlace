package com.marketPlace.itemstorageservice.controller;

import com.marketPlace.itemstorageservice.DTOmodels.ItemDetailedInfoDTO;
import com.marketPlace.itemstorageservice.services.ItemDetailedDTOService;
import com.marketPlace.itemstorageservice.services.ItemService;
import com.marketPlace.itemstorageservice.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@Validated
public class ItemDTOController {

    ItemService itemService;
    ItemDetailedDTOService itemDetailedService;

    @Autowired
    public ItemDTOController(ItemService itemService, ItemDetailedDTOService itemDetailedService) {
        this.itemService = itemService;
        this.itemDetailedService = itemDetailedService;
    }

    @GetMapping("/itemsDTO")
    public ResponseEntity<List<ItemDetailedInfoDTO>> allDTOItems(){
        HttpHeaders headers = Utils.httpHeader("list of all itemsDTO","");
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(itemDetailedService.allItems());
    }

    @GetMapping("/itemsDTO/{id}")
    public ResponseEntity<ItemDetailedInfoDTO> getItemDTO(@Valid @PathVariable("id") Long id){
        Optional<ItemDetailedInfoDTO> itemDTO = itemDetailedService.getUpdatedItemDetailedDTO();
        HttpHeaders headers = Utils.httpHeader("itemDTO","");
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(itemDTO.isEmpty() ? null : itemDTO.get());
    }
}