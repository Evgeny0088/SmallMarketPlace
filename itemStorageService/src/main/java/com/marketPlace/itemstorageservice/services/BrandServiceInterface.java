package com.marketPlace.itemstorageservice.services;

import com.marketPlace.itemstorageservice.models.BrandName;

import java.util.List;

public interface BrandServiceInterface {

    List<BrandName> allBrands();
    BrandName postNewBrandName(BrandName brandName);
    void updateBrand(String brandDB, BrandName brand);
    String brandDeleted(Long id);
}
