package com.marketplace.itemstorageservice.backupTests;

import com.marketplace.itemstorageservice.models.BrandName;
import com.marketplace.itemstorageservice.repositories.BrandNameRepo;

import com.marketplace.itemstorageservice.exceptions.CustomItemsException;
import com.marketplace.itemstorageservice.services.BrandServiceImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@Disabled
@ExtendWith(MockitoExtension.class)
public class BrandServiceTest_BACKUPTest {

    @Mock
    private BrandNameRepo brandNameRepo;

    @InjectMocks
    BrandServiceImpl brandService;

    @Test
    @DisplayName("find all brands")
    void allBrandsTest(){
        List<BrandName> brands = List.of(new BrandName("b1","0.1"),
                new BrandName("b2","0.1"),
                new BrandName("b3","0.1")
                );
        given(brandNameRepo.findAll()).willReturn(brands);
        List<BrandName> excepted = brandService.allBrands();
        assertEquals(brands,excepted);
    }

    @Test
    @DisplayName("create new Brand name with valid inputs")
    void postNewBrandName() {
        String brandName = "new";
        final BrandName newBrand = new BrandName();
        newBrand.setId(1L);
        newBrand.setName(brandName);
        newBrand.setVersion("0.1");
        given(brandNameRepo.findByName(brandName)).willReturn(null);
        given(brandNameRepo.save(newBrand)).willAnswer(invocation -> invocation.getArgument(0));

        brandService.postNewBrandName(newBrand);

        verify(brandNameRepo, times(1)).save(Mockito.any(BrandName.class));
    }

    @Test
    @DisplayName("failed to save if Brand from DB with the same name found")
    void postBrandNameFailedTest(){
        String brandName = "brandFromDB";
        BrandName brand = new BrandName(brandName, "0.1");

        given(brandNameRepo.findByName(brandName)).willReturn(brand);

        assertThrows(CustomItemsException.class,()->brandService.postNewBrandName(brand));
        verify(brandNameRepo, never()).save(Mockito.any(BrandName.class));
    }

    @Test
    @DisplayName("update existing Brand in DB")
    void updateBrandTest(){
        String brandName = "brandFromDB";
        BrandName brand = new BrandName(brandName, "0.1");
        given(brandNameRepo.findByName(brandName)).willReturn(brand);
        given(brandNameRepo.save(brand)).willAnswer(invocation -> invocation.getArgument(0));
        brandService.updateBrand(brandName, brand);

        assertNotNull(brand);
        verify(brandNameRepo, times(1)).save(Mockito.any(BrandName.class));
    }

    @Test
    @DisplayName("failed to update if Brand from DB not found")
    void updateBrandFailedTest(){
        String brandName = "brandFromDB";
        BrandName brand = new BrandName(brandName, "0.1");

        given(brandNameRepo.findByName(brandName)).willReturn(null);

        assertThrows(CustomItemsException.class,()->brandService.updateBrand(brandName,brand));
        verify(brandNameRepo, never()).save(Mockito.any(BrandName.class));
    }

    @Test
    @DisplayName("delete Brand")
    void brandDeletedTest(){
        Long id = 1L;
        BrandName deletedBrand = new BrandName("brand", "0.1");
        deletedBrand.setId(id);
        given(brandNameRepo.findById(id)).willReturn(Optional.of(deletedBrand));
        brandService.brandDeleted(id);
        verify(brandNameRepo,times(1)).deleteById(id);
    }

    @Test
    @DisplayName("failed to delete Brand")
    void brandDeletedFailedTest(){
        Long id = 1L;
        given(brandNameRepo.findById(anyLong())).willReturn(Optional.empty());
        assertThrows(CustomItemsException.class,()->brandService.brandDeleted(anyLong()));
        verify(brandNameRepo, never()).deleteById(id);
    }

}

