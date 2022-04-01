package com.marketplace.itemstorageservice.services;

import com.marketplace.itemstorageservice.configs.BrandServiceTestConfig;
import com.marketplace.itemstorageservice.exceptions.CustomItemsException;
import com.marketplace.itemstorageservice.models.BrandName;
import com.marketplace.itemstorageservice.repositories.BrandNameRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = BrandServiceTestConfig.Initializer.class, classes = {BrandServiceTestConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BrandServiceTest {

    @Autowired
    BrandNameRepo brandNameRepo;

    @Autowired
    BrandService brandService;

    @Test
    @DisplayName("post brands and get all brands from database")
    void getAllItems(){
        //given
        IntStream.of(3).forEach(i->brandService.postNewBrandName(new BrandName(String.format("brand%s",i),"100")));
        //when
        List<BrandName> allBrands = brandService.allBrands();
        //then
        assertEquals(3, allBrands.size());
    }

    @Test
    @DisplayName("post brand and verify that existed brand will not be persisted again")
    void postNewBrand(){
        String brandName = "brand";
        //given
        BrandName brand = new BrandName(brandName, "100");
        //when
        brandService.postNewBrandName(brand);
        BrandName brandFromDB = brandNameRepo.findByName(brand.getName());
        //then
        assertNotNull(brandFromDB);
        assertEquals(brandFromDB.getName(), brand.getName());

        //and then

//        given(otherServiceMock.bar()).willThrow(new MyException());
//
//        when(() -> myService.foo());
//
//        then(caughtException()).isInstanceOf(MyException.class);

        given(brandNameRepo.findByName(brandName)).willReturn(brand);

        if (brandFromDB.getName().equals(brand.getName())){
            assertThrows(CustomItemsException.class,()-> mock(BrandService.class).postNewBrandName(brand));
        }
//        verify(brandNameRepo, never()).save(Mockito.any(BrandName.class));
    }


}

