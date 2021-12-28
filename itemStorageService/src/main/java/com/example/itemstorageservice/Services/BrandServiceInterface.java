package com.example.itemstorageservice.Services;

import com.example.itemstorageservice.Models.BrandName;

import java.util.List;

public interface BrandServiceInterface {

    List<BrandName> allBrands();
    BrandName postNewBrandName(BrandName brandName);
    void updateBrand(String brandDB, BrandName brand);
    String brandDeleted(Long id);
    void pageOpenAllBrandsStatistic();
}
