package com.marketplace.itemstorageservice.controller;

import com.marketplace.itemstorageservice.models.BrandName;
import com.marketplace.itemstorageservice.services.BrandService;
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
@RequestMapping("brands")
@Validated
public class BrandController {

    private final BrandService brandService;
    private static final String inputsFoNewBrand = """
                {
                    "name": "abibas",
                    "version": "0.2"
                }
            """;

    @Autowired
    public BrandController(BrandService brandService) {
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

    @Operation(summary = "create new brand in database",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "brandName inputs for new brand creation in database",
                    required = true,
                    content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = inputsFoNewBrand))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "create new brand in database",
                    content = {@Content(mediaType = "text/plain")}),
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

    @Operation(summary = "update existed brand",
                requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                        description = "brandName inputs for updated brand in database",
                        required = true,
                        content = @Content(mediaType = "application/json",
                        examples = @ExampleObject(value = inputsFoNewBrand))),
                parameters = {@Parameter(name = "brand_name",
                        description = "brand name for searching existed brandName object in database",
                        schema = @Schema(type = "string"),
                        required = true,
                        example = "Abibas")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "brand is updated in database",
                    content = @Content(mediaType = "text/plain",
                    examples = @ExampleObject(value = inputsFoNewBrand))),
            @ApiResponse(responseCode = "400",
                    description = """
                                    There are a few reasons to get bad request:
                                    1. Brand name must be unique
                                    2. Brand name is missing as request body
                                    3. BrandName object wrongly specified in Request Body, check your inputs
                                    """,
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/update")
    public ResponseEntity<String> updateBrand(@Valid @RequestParam("brand_name") String brandDB, @Valid @RequestBody BrandName brandName){
        brandService.updateBrand(brandDB,brandName);
        HttpHeaders headers = Utils.httpHeader("update brand","");
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(String.format("brand with name %s successfully updated", brandDB));
    }

    @Operation(summary = "delete existed brand by id",
            parameters = {@Parameter(name = "id",
                    description = "id of brand, used for searching in db",
                    schema = @Schema(type = "integer"),
                    required = true,
                    example = "1")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = """
                            delete BrandName AND all references connected to it,
                            it means all items references to this brand also will be deleted!
                            """,
                    content = {@Content(mediaType = "text/plain")}),
            @ApiResponse(responseCode = "400",
                    description = """
                                    There are a few reasons to get bad request:
                                    1. BrandName object cannot be found by specified id
                                    2. Type of specified id is not digit (exception to be thrown)
                                    3. Id parameter is empty in request
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