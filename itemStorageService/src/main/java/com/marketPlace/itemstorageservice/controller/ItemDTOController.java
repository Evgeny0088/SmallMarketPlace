package com.marketPlace.itemstorageservice.controller;

import com.marketPlace.itemstorageservice.DTOmodels.ItemDetailedInfoDTO;
import com.marketPlace.itemstorageservice.DTOmodels.ItemSoldDTO;
import com.marketPlace.itemstorageservice.services.ItemDetailedDTOServiceImpl;
import com.marketPlace.itemstorageservice.services.ItemServiceImpl;
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

    ItemServiceImpl itemService;
    ItemDetailedDTOServiceImpl itemDetailedService;

    @Autowired
    public ItemDTOController(ItemServiceImpl itemService, ItemDetailedDTOServiceImpl itemDetailedService) {
        this.itemService = itemService;
        this.itemDetailedService = itemDetailedService;
    }

    @GetMapping("/itemsDTO")
    public ResponseEntity<List<ItemDetailedInfoDTO>> allDTOItems(){
        HttpHeaders headers = Utils.httpHeader("list of all itemsDTO","");
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(itemDetailedService.allItems());
    }

    @GetMapping("/itemsDTO/{id}")
    public ResponseEntity<String> getItemDTO(@Valid @PathVariable("id") Long id){
        ItemDetailedInfoDTO itemDTO = itemDetailedService.getUpdatedItemDetailedDTO(id);
        HttpHeaders headers = Utils.httpHeader("itemDTO","");
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(itemDTO != null ? itemDTO.toString() : "no updated packages!");
    }

    @RequestMapping(method = RequestMethod.GET, value = "itemDTO/deleteSoldItems/{id}")
    public ResponseEntity<String> itemIsDeleted(@Valid @PathVariable("id") Long package_id,
                                                @Valid @RequestParam("count") int count){
        ItemSoldDTO request = new ItemSoldDTO(package_id, count);
        HttpHeaders headers = Utils.httpHeader("delete item","item is deleted from DB");
        int removedItems = itemDetailedService.removeItemsFromPackage(request);
        ItemDetailedInfoDTO updatedItem = itemDetailedService.getUpdatedItemDetailedDTO(package_id);
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(removedItems > 0 ? updatedItem.toString() : "package was not updated!");
    }
}