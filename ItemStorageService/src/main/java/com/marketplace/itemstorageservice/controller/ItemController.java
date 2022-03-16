package com.marketplace.itemstorageservice.controller;

import com.marketplace.itemstorageservice.models.Item;
import com.marketplace.itemstorageservice.services.ItemService;
import com.marketplace.itemstorageservice.utils.Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("items")
@Validated
public class ItemController {

    private final ItemService itemService;
    private static final String inputsFoNewItem = """
            {
                "serial": 100,
                "brandName": {
                    "id":110
                            },
                "parentItem": {
                    "id":100
                },
                "item_type": "ITEM"
                }
            """;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @Operation(summary = "fetch all items from database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "fetch all items from database",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400",
                    description = "bad request",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping
    public ResponseEntity<List<Item>> allItems(){
        HttpHeaders headers = Utils.httpHeader("list of all items","");
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(itemService.allItems());
    }

    @Operation(summary = "create new item in database",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "brandName inputs for new brand creation in database",
                    required = true,
                    content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = inputsFoNewItem))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "create new item in database",
                    content = {@Content(mediaType = "text/plain")}),
            @ApiResponse(responseCode = "400",
                    description = """
                                    There are a few reasons to get bad request:
                                    1. Null parent allowed only when item_type is PACK for new item, otherwise throws error
                                    2. BrandName id for new item must be equal to parent (parentItem if exists)
                                    """,
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/new")
    public ResponseEntity<String> newItem(@Valid @RequestBody Item newItem){
        itemService.createNewItem(newItem);
        HttpHeaders headers = Utils.httpHeader("new item creation","Adding new item into DB");
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body("new Item is created!");
    }

    @Operation(summary = "update existed item",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "updated item object instead of item in database",
                    required = true,
                    content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = inputsFoNewItem))),
            parameters = {@Parameter(name = "itemId",
                    description = "Item id for searching existed item object in database",
                    schema = @Schema(type = "integer"),
                    required = true,
                    example = "1")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "update existed item",
                    content = {@Content(mediaType = "text/plain")}),
            @ApiResponse(responseCode = "400",
                    description = """
                                    There are a few reasons to get bad request:
                                    1. Item must have a PACK type if do not have any parent
                                    2. BrandName reference must be the same for item and parent
                                    3. Other input errors, please check inputs carefully for updated item 
                                    """,
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/update")
    public ResponseEntity<String> updateItem(@Valid @RequestParam("itemId") long id, @Valid @RequestBody Item item){
        itemService.updateItem(id,item);
        HttpHeaders headers = Utils.httpHeader("update item", String.format("item id: %d", id));
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(String.format("item with %d successfully updated", id));
    }

    @Operation(summary = "delete existed item by id",
            parameters = {@Parameter(name = "id",
                    description = "item id for searching in of existed item in database",
                    schema = @Schema(type = "integer"),
                    required = true,
                    example = "1")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = """
                            delete item AND all references connected to it,
                            it means if item had child items inside those items also will be deleted!
                            Also, deletion process is recursive:
                            it means that if deleted item is last in list of parent item, then parent item also will be deleted!           
                            """,
                    content = {@Content(mediaType = "text/plain")}),
            @ApiResponse(responseCode = "400",
                    description = """
                                    There are a few reasons to get bad request:
                                    1. Item object cannot be found by specified id
                                    """,
                    content = @Content(mediaType = "application/json"))
    })
    @RequestMapping(method = RequestMethod.GET, value = "/delete/{id}")
    public ResponseEntity<String> itemIsDeleted(@Valid @PathVariable("id") Long id){
        HttpHeaders headers = Utils.httpHeader("delete item","item is deleted from DB");
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(itemService.itemDeleted(id));
    }
}