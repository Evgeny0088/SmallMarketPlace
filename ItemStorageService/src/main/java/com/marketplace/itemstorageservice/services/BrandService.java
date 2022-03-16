package com.marketplace.itemstorageservice.services;

import com.marketplace.itemstorageservice.models.BrandName;

import java.util.List;

public interface BrandService {

    List<BrandName> allBrands();
    BrandName postNewBrandName(BrandName brandName);
    void updateBrand(String brandDB, BrandName brand);
    String brandDeleted(Long id);
}
