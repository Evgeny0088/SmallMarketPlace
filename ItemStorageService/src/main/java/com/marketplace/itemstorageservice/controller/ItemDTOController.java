package com.marketplace.itemstorageservice.controller;

import com.marketplace.itemstorageservice.DTOmodels.ItemDetailedInfoDTO;
import com.marketplace.itemstorageservice.DTOmodels.ItemSoldDTO;
import com.marketplace.itemstorageservice.services.ItemDetailedDTOService;
import com.marketplace.itemstorageservice.services.ItemDetailedDTOServiceImpl;
import com.marketplace.itemstorageservice.services.ItemService;
import com.marketplace.itemstorageservice.services.ItemServiceImpl;
import com.marketplace.itemstorageservice.utils.Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("itemsDTO")
@Validated
public class ItemDTOController {

    ItemService itemService;
    ItemDetailedDTOService<ItemDetailedInfoDTO,ItemSoldDTO> itemDetailedService;

    @Autowired
    public ItemDTOController(ItemServiceImpl itemService, ItemDetailedDTOServiceImpl itemDetailedService) {
        this.itemService = itemService;
        this.itemDetailedService = itemDetailedService;
    }

    @Operation(summary = "fetch all DTO items from database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "fetch all DTO items from database",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400",
                    description = "bad request",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping
    public ResponseEntity<List<ItemDetailedInfoDTO>> allDTOItems(){
        HttpHeaders headers = Utils.httpHeader("list of all itemsDTO","");
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(itemDetailedService.allItems());
    }

    @Operation(summary = "Get specific itemDTO from database by specified id",
            parameters = {@Parameter(name = "id",
                    description = "item id for searching in of existed item in database",
                    schema = @Schema(type = "integer"),
                    required = true,
                    example = "1")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = """
                            In order to fetch ItemDTO from db, native SQL request is used,
                            it mapped on ItemDetailedInfoDTO.class
                            Main idea is to fetch item and children (count of items reference to it with item_type=ITEM)
                            - In case if fetched item do not have children then it returns 0 count for this field
                            - In case if fetched item has item_type=ITEM then it throws message that it is not a package!
                            """,
                    content = {@Content(mediaType = "text/plain")}),
            @ApiResponse(responseCode = "400",
                    description = """
                                    There are a few reasons to get bad request:
                                    1. Failed to map on DTO object
                                    2. SQL is not correct
                                    """,
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/{id}")
    public ResponseEntity<String> getItemDTO(@Valid @PathVariable("id") Long id){
        ItemDetailedInfoDTO itemDTO = itemDetailedService.getUpdatedItemDetailedDTO(id);
        HttpHeaders headers = Utils.httpHeader("itemDTO","");
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(itemDTO != null ? itemDTO.toString() : "package with specified id does not exists!");
    }

    @Operation(summary = "delete specified count of items in database",
            parameters = {@Parameter(name = "id",
                    description = "Item id for searching existed item object in database",
                    schema = @Schema(type = "integer"),
                    required = true,
                    example = "1"),
            @Parameter(name = "count",
                    description = "Count of items tb deleted from database",
                    schema = @Schema(type = "integer"),
                    required = true,
                    example = "1")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = """
                            Main idea is to fetch item package (parent item) by id and remove specified count of items(parent children) from database
                            - In case if:
                                - fetched item do not have children
                                - specified count is greater then actual children count in database
                                - specified package is not found in database
                            then it throws a message that package is not valid or empty
                            """,
                    content = {@Content(mediaType = "text/plain")}),
            @ApiResponse(responseCode = "400",
                    description = """
                                    There are a few reasons to get bad request:
                                    1. id or count parameter type is not valid or missed
                                    """,
                    content = @Content(mediaType = "application/json"))
    })
    @RequestMapping(method = RequestMethod.GET, value = "/deleteSoldItems/{id}")
    public ResponseEntity<String> itemIsDeleted(@Valid @PathVariable("id") Long package_id,
                                                @Valid @RequestParam("count") int count){
        ItemSoldDTO request = new ItemSoldDTO(package_id, count);
        int removedItems = itemDetailedService.removeItemsFromPackage(request);
        ItemDetailedInfoDTO updatedItem = itemDetailedService.getUpdatedItemDetailedDTO(package_id);
        HttpHeaders headers = Utils.httpHeader("sold items","sold items are deleted from DB");
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(removedItems > 0 ? updatedItem.toString() : "package is empty or not valid!...");
    }
}