package com.example.itemstorageservice.Services;

import com.example.itemstorageservice.Models.BrandName;
import com.example.itemstorageservice.Models.PageStatistic;
import com.example.itemstorageservice.Repositories.BrandNameRepo;
import com.example.itemstorageservice.Repositories.PageStatisticRepo;
import com.example.itemstorageservice.exceptions.CustomItemsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BrandService implements BrandServiceInterface{

    private final BrandNameRepo brandNameRepo;

    @Autowired
    PageStatisticRepo pageStatisticRepo;

    @Autowired
    public BrandService(BrandNameRepo brandNameRepo) {
        this.brandNameRepo = brandNameRepo;
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
    public void pageOpenAllBrandsStatistic(){
        pageStatisticRepo.save(new PageStatistic(LocalDateTime.now()));
    }

    @Override
    public void updateBrand(String brandDB, BrandName brand){
        BrandName brandFromDB = brandNameRepo.findByName(brandDB);
        if (brandFromDB!=null){
            brandFromDB.setName(brand.getName());
            brandFromDB.setVersion(brand.getVersion());
            brandFromDB.setCreateDate(LocalDateTime.now());
            brandNameRepo.save(brandFromDB);
        }else {
            throw new CustomItemsException(brandDB,"not possible to update, brand with this name not found");
        }
    }

    @Override
    public String brandDeleted(Long id){
        if (brandNameRepo.findById(id).isPresent()){
            brandNameRepo.deleteById(id);
            return String.format("brand with %d is deleted!", id) ;
        }else {
            throw new CustomItemsException("brand with id: " +
                    id, "is not found");
        }
    }
}
