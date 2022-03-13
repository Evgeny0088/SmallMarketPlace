package com.marketPlace.itemstorageservice.controller;

import com.marketPlace.itemstorageservice.models.BrandName;
import com.marketPlace.itemstorageservice.services.BrandServiceImpl;
import com.marketPlace.itemstorageservice.utils.Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
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
@RequestMapping("brands")
@Validated
public class BrandController {

    private final BrandServiceImpl brandService;

    @Autowired
    public BrandController(BrandServiceImpl brandService) {
        this.brandService = brandService;
    }

    @Operation(summary = "fetch all brands and attached items(PACK/ITEM items) from database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
            description = "fetch all brands from database with attached items",
            content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400",
                    description = "bad request",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping
    public ResponseEntity<List<BrandName>> allBrands(){
        HttpHeaders header = Utils.httpHeader("all brands","list of all brands");
        return ResponseEntity.status(HttpStatus.OK).headers(header).body(brandService.allBrands());
    }

    @Operation(summary = "create new brand in database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "create new brand in database",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400",
                    description = """
                                    There are a few reasons to get bad request:
                                    1. Brand name must be unique
                                    """,
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/new")
    public ResponseEntity<String> newBrand(@Valid @RequestBody BrandName newBrand){
        BrandName savedBrand = brandService.postNewBrandName(newBrand);
        HttpHeaders headers = Utils.httpHeader("adding new brand in DB","");
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(String.format("new brand successfully added with id %d!", savedBrand.getId()));
    }

    @Operation(summary = "update existed brand")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "brand is updated in database",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400",
                    description = """
                                    There are a few reasons to get bad request:
                                    1. Brand name must be unique
                                    """,
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/update")
    public ResponseEntity<String> updateBrand(@Valid @RequestParam("brand_name") String brandDB, @Valid @RequestBody BrandName brandName){
        brandService.updateBrand(brandDB,brandName);
        HttpHeaders headers = Utils.httpHeader("update brand","");
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(String.format("brand with name %s successfully updated", brandDB));
    }

    @Operation(summary = "delete existed brand by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "brand is deleted in database",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400",
                    description = """
                                    There are a few reasons to get bad request:
                                    1. specified id does not exist
                                    """,
                    content = @Content(mediaType = "application/json"))
    })
    @RequestMapping(method = RequestMethod.GET, value = "/delete/{id}")
    public ResponseEntity<String> brandIsDeleted(@PathVariable("id") Long id){
        String responseBody = brandService.brandDeleted(id);
        HttpHeaders headers = Utils.httpHeader("delete brand","item is deleted from DB");
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(responseBody);
    }
}