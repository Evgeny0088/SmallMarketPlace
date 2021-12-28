package com.example.itemstorageservice.controller;

import com.example.itemstorageservice.Models.BrandName;
import com.example.itemstorageservice.Services.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;
import static com.example.itemstorageservice.utils.Utils.httpHeader;

@RestController
@Validated
public class BrandController {

    @Autowired
    BrandService brandService;

    @GetMapping("/brands")
    public ResponseEntity<List<BrandName>> allBrands(){
        HttpHeaders header = httpHeader("all brands","list of all brands");
        ResponseEntity<List<BrandName>> brands = ResponseEntity.status(HttpStatus.OK).headers(header).body(brandService.allBrands());
        if (brands.getStatusCode().is2xxSuccessful()){
            brandService.pageOpenAllBrandsStatistic();
        }
        return brands;
    }

    @PostMapping("/newBrand")
    public ResponseEntity<String> newBrand(@Valid @RequestBody BrandName newBrand){
        BrandName savedBrand = brandService.postNewBrandName(newBrand);
        HttpHeaders headers = httpHeader("adding new brand in DB","");
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(String.format("new brand successfully added with id %d!", savedBrand.getId()));
    }

    @PostMapping("brand/update")
    public ResponseEntity<String> updateBrand(@Valid @RequestParam("brand_name") String brandDB, @Valid @RequestBody BrandName brandName){
        brandService.updateBrand(brandDB,brandName);
        HttpHeaders headers = httpHeader("update brand","");
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(String.format("brand with name %s successfully updated", brandDB));
    }

    @RequestMapping(method = RequestMethod.GET, value = "brand/delete/{id}")
    public ResponseEntity<String> brandIsDeleted(@PathVariable("id") Long id){
        String responseBody = brandService.brandDeleted(id);
        HttpHeaders headers = httpHeader("delete brand","item is deleted from DB");
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(responseBody);
    }
}