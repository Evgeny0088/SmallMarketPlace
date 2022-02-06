package com.marketPlace.itemstorageservice.controller;

import com.marketPlace.itemstorageservice.models.BrandName;
import com.marketPlace.itemstorageservice.services.BrandService;
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
public class BrandController {

    @Autowired
    BrandService brandService;

    @GetMapping("/brands")
    public ResponseEntity<List<BrandName>> allBrands(){
        HttpHeaders header = Utils.httpHeader("all brands","list of all brands");
        return ResponseEntity.status(HttpStatus.OK).headers(header).body(brandService.allBrands());
    }

    @PostMapping("/newBrand")
    public ResponseEntity<String> newBrand(@Valid @RequestBody BrandName newBrand){
        BrandName savedBrand = brandService.postNewBrandName(newBrand);
        HttpHeaders headers = Utils.httpHeader("adding new brand in DB","");
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(String.format("new brand successfully added with id %d!", savedBrand.getId()));
    }

    @PostMapping("brand/update")
    public ResponseEntity<String> updateBrand(@Valid @RequestParam("brand_name") String brandDB, @Valid @RequestBody BrandName brandName){
        brandService.updateBrand(brandDB,brandName);
        HttpHeaders headers = Utils.httpHeader("update brand","");
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(String.format("brand with name %s successfully updated", brandDB));
    }

    @RequestMapping(method = RequestMethod.GET, value = "brand/delete/{id}")
    public ResponseEntity<String> brandIsDeleted(@PathVariable("id") Long id){
        String responseBody = brandService.brandDeleted(id);
        HttpHeaders headers = Utils.httpHeader("delete brand","item is deleted from DB");
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(responseBody);
    }
}