package com.marketplace.itemstorageservice.services;

import com.marketplace.itemstorageservice.models.BrandName;
import com.marketplace.itemstorageservice.repositories.BrandNameRepo;
import com.marketplace.itemstorageservice.exceptions.CustomItemsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class BrandServiceImpl implements BrandService {

    private final BrandNameRepo brandNameRepo;

    @Autowired
    public BrandServiceImpl(BrandNameRepo brandNameRepo) {
        this.brandNameRepo = brandNameRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandName> allBrands(){
        return brandNameRepo.findAll();
    }

    @Override
    @Transactional(rollbackFor = CustomItemsException.class)
    public BrandName postNewBrandName(BrandName brandName){
        if (brandNameRepo.findByName(brandName.getName()) == null){
            brandName.setCreateDate(LocalDateTime.now());
            brandNameRepo.save(brandName);
            return brandName;
        }else {
            String errorMessage = String.format("<%s>Already exists in DB...",brandName);
            log.error(errorMessage);
            throw new CustomItemsException(brandName.getName(),errorMessage);
        }
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = CustomItemsException.class)
    public void updateBrand(String brandDB, BrandName brand){
        BrandName brandFromDB = brandNameRepo.findByName(brandDB);
        if (brandFromDB!=null){
            brandFromDB.setName(brand.getName());
            brandFromDB.setVersion(brand.getVersion());
            brandFromDB.setCreateDate(LocalDateTime.now());
            brandNameRepo.save(brandFromDB);
        }else {
            String errorMessage = String.format("Not possible to update <%s> , brand with this name not found...",brandDB);
            log.error(errorMessage);
            throw new CustomItemsException(brandDB,errorMessage);
        }
    }

    @Override
    @Transactional(rollbackFor = CustomItemsException.class)
    public String brandDeleted(Long id){
        BrandName brandToRemove = brandNameRepo.findById(id).orElse(null);
        if (brandToRemove != null){
            brandNameRepo.deleteById(id);
            return String.format("brand with %d is deleted!", id) ;
        }else {
            String errorMessage = String.format("Not possible to delete <%d> , brand with this id not found...",id);
            log.error(errorMessage);
            throw new CustomItemsException(id,errorMessage);
        }
    }
}
