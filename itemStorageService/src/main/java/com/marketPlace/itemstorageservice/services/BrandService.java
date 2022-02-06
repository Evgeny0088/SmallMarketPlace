package com.marketPlace.itemstorageservice.services;

import com.marketPlace.itemstorageservice.models.BrandName;
import com.marketPlace.itemstorageservice.repositories.BrandNameRepo;
import com.marketPlace.itemstorageservice.exceptions.CustomItemsException;
import org.ehcache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BrandService implements BrandServiceInterface{

    private final BrandNameRepo brandNameRepo;
    private final Cache<Long, BrandName> brandCache;

    @Autowired
    public BrandService(BrandNameRepo brandNameRepo, Cache<Long, BrandName> brandCache) {
        this.brandNameRepo = brandNameRepo;
        this.brandCache = brandCache;
    }

    public List<BrandName> allBrands(){
        return brandNameRepo.findAll();
    }

    @Override
    public BrandName postNewBrandName(BrandName brandName){
        if (brandNameRepo.findByName(brandName.getName()) == null){
            brandName.setCreateDate(LocalDateTime.now());
            brandNameRepo.save(brandName);
            return brandNameRepo.getById(brandName.getId());
        }else {
            throw new CustomItemsException(brandName.getName(),"already exists in DB");
        }
    }

    @Override
    public void updateBrand(String brandDB, BrandName brand){
        BrandName brandFromDB = brandNameRepo.findByName(brandDB);
        if (brandFromDB!=null){
            brandFromDB.setName(brand.getName());
            brandFromDB.setVersion(brand.getVersion());
            brandFromDB.setCreateDate(LocalDateTime.now());
            brandNameRepo.save(brandFromDB);
            brandCache.put(brandFromDB.getId(), brandFromDB);
        }else {
            throw new CustomItemsException(brandDB,"not possible to update, brand with this name not found");
        }
    }

    @Override
    public String brandDeleted(Long id){
        BrandName brandToRemove = brandNameRepo.findById(id).orElse(null);
        if (brandToRemove != null){
            brandNameRepo.deleteById(id);
            brandCache.remove(id);
            return String.format("brand with %d is deleted!", id) ;
        }else {
            throw new CustomItemsException("brand with id: " +
                    id, "is not found");
        }
    }
}
