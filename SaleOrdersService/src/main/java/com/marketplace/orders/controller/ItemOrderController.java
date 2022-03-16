package com.marketplace.orders.controller;

import com.marketplace.orders.DTOModels.ItemDetailedInfoDTO;
import com.marketplace.orders.service.ItemProducerService;
import com.marketplace.orders.service.PageStatisticService;
import com.marketplace.orders.service.PageStatisticServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.ehcache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

import static com.marketplace.orders.utils.Utils.httpHeader;

@RestController
@Validated
public class ItemOrderController {

    private final PageStatisticService pageStatisticService;
    private final ItemProducerService itemSoldProducerService;
    private final Cache<Long, ItemDetailedInfoDTO> itemDTOCache;

    private static final String notFoundResponse = """
            {
                "timestamp": "yyyy-MM-DDTHH:MM:SS",
                "status": 404,
                "error": "Not Found",
                "message": "No message available",
                "path": "/saleorders/api/v1/brandsMainPag"
            }
            """;

    private static final String warningMessage = "failed to sold items, probably not enough items in package!...";

    @Autowired
    public ItemOrderController(PageStatisticServiceImpl pageStatisticService,
                               ItemProducerService itemSoldProducerService,
                               Cache<Long, ItemDetailedInfoDTO> itemDTOCache) {
        this.pageStatisticService = pageStatisticService;
        this.itemSoldProducerService = itemSoldProducerService;
        this.itemDTOCache = itemDTOCache;
    }

    @Operation(summary = "fetch all itemDetailedDTO packages available at this service >>> taken from local service cache")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "fetch all DTO items from database",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404",
                    description = "not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = notFoundResponse)))
    })
    @GetMapping("/brandsMainPage")
    public ResponseEntity<List<ItemDetailedInfoDTO>> allBrands(){
        HttpHeaders header = httpHeader("all package brands","list of all packages available for brands");
        List<ItemDetailedInfoDTO> packages = new ArrayList<>();
        itemDTOCache.forEach(entry->packages.add(entry.getValue()));
        ResponseEntity<List<ItemDetailedInfoDTO>> brands = ResponseEntity.status(HttpStatus.OK).headers(header).body(packages);
        if (brands.getStatusCode().is2xxSuccessful()){
            pageStatisticService.countOpenMainPages();
        }
        return brands;
    }

    @Operation(summary = "request package id and count of sold items in it",
            parameters = {@Parameter(name = "id",
                    description = "package id where sold items should be removed",
                    schema = @Schema(type = "integer"),
                    required = true,
                    example = "1"),
            @Parameter(name = "quantity",
                    description = "Count of sold items which should be removed from specified package",
                    schema = @Schema(type = "integer"),
                    required = true,
                    example = "2")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = """
                            throw info message if possible to sold such quantity in package, 
                            or if specified quantity too large then it warning message will be thrown!  
                            see example below                      
                            """,
                    content = {@Content(mediaType = "text/plain",
                    examples = @ExampleObject(value = warningMessage))}),
            @ApiResponse(responseCode = "400",
                    description = """
                                    There are a few reasons to get bad request:
                                    1. if package id does not exist at the service
                                    then exception will be thrown
                                    """,
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/itemSold/{id}")
    public ResponseEntity<String> itemSold(@Valid @PathVariable("id") Long id, @Valid @RequestParam(value = "quantity") int quantity){
        HttpHeaders header = httpHeader("item is sold",String.format("item with id:%d",id));
        if (itemSoldProducerService.sendSoldItem(id,quantity)){
            return ResponseEntity.status(HttpStatus.OK).headers(header).body("items are sold!...");
        }else {
            return ResponseEntity.status(HttpStatus.OK).headers(header).body("failed to sold items, probably not enough items in package!...");
        }
    }
}
