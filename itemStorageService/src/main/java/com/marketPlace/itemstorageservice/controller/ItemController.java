package com.marketPlace.itemstorageservice.controller;

import com.marketPlace.itemstorageservice.models.Item;
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

@RestController
@Validated
public class ItemController {

    @Autowired
    ItemServiceImpl itemService;

    @Autowired
    ItemDetailedDTOServiceImpl itemDetailedService;

    @GetMapping("/items")
    public ResponseEntity<List<Item>> allItems(){
        HttpHeaders headers = Utils.httpHeader("list of all items","");
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(itemService.allItems());
    }

    @PostMapping("/newItem")
    public ResponseEntity<String> newItem(@Valid @RequestBody Item newItem){
        itemService.createNewItem(newItem);
        HttpHeaders headers = Utils.httpHeader("new item creation","Adding new item into DB");
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body("new Item is created!");
    }

    @PostMapping("item/update")
    public ResponseEntity<String> updateItem(@Valid @RequestParam("itemId") long id, @Valid @RequestBody Item item){
        itemService.updateItem(id,item);
        HttpHeaders headers = Utils.httpHeader("update item", String.format("item id: %d", id));
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(String.format("item with %d successfully updated", id));
    }

    @RequestMapping(method = RequestMethod.GET, value = "item/delete/{id}")
    public ResponseEntity<String> itemIsDeleted(@Valid @PathVariable("id") Long id){
        HttpHeaders headers = Utils.httpHeader("delete item","item is deleted from DB");
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(itemService.itemDeleted(id));
    }
}