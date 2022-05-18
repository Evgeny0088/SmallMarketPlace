package com.marketplace.itemstorageservice.services;

import com.marketplace.itemstorageservice.configs.ServiceTestConfig;
import com.marketplace.itemstorageservice.exceptions.CustomItemsException;
import com.marketplace.itemstorageservice.models.BrandName;
import com.marketplace.itemstorageservice.repositories.BrandNameRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.marketplace.itemstorageservice.utilFunctions.HelpTestFunctions.brandsPersist;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = ServiceTestConfig.Initializer.class, classes = {ServiceTestConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BrandServiceTest {

    @Autowired
    BrandNameRepo brandNameRepo;
    @Autowired
    BrandService brandService;

    @Order(0)
    @Test
    @Transactional
    @DisplayName("post brands and get all brands from database")
    void getAllBrands(){
        //given
        brandsPersist(brandService);
        //when
        List<BrandName> allBrands = brandService.allBrands();
        //then
        assertEquals(3, allBrands.size());
    }

    @Order(1)
    @Test
    @DisplayName("post brand and verify that brand with the same name won,t be persisted again")
    void postNewBrand(){
        String brandName = "brand";
        //given
        BrandName brand = new BrandName(brandName, "100");
        //when
        brandService.postNewBrandName(brand);
        BrandName brandFromDB = brandNameRepo.findByName(brand.getName());
        //then
        assertNotNull(brandFromDB);
        assertThat(brand.getName()).isEqualTo(brandFromDB.getName());

        //######### test case when repeatable saving is failed #############
        //given
        BrandNameRepo brandNameRepoMock = mock(BrandNameRepo.class);
        BrandServiceImpl brandServiceMock = spy(new BrandServiceImpl(brandNameRepoMock));
        //when
        when(brandNameRepoMock.findByName(brandName)).thenReturn(brand);
        //then
        assertThrows(CustomItemsException.class,()-> brandServiceMock.postNewBrandName(brand));
        verify(brandNameRepoMock, never()).save(any(BrandName.class));
    }

    @Order(2)
    @DisplayName("update brand if exists and if request inputs are correct")
    @ParameterizedTest(name = "test case: => brand={0}")
    @CsvSource({"brandDBExisted, updatedBrand"})
    void updateBrandTest(String brandFromDB, String updatedBrand) {
        //given -> we persist brand in database
        brandService.postNewBrandName(new BrandName(brandFromDB, "100"));
        BrandName brandDB = brandNameRepo.findByName(brandFromDB);
        assertNotNull(brandDB);
        //then -> update all params and save it again in DB
        brandDB.setName(updatedBrand);
        brandDB.setVersion("200");
        brandDB.setCreateDate(LocalDateTime.now());
        brandNameRepo.save(brandDB);

        //then -> get updated brand from database and make sure that is not null
        //then -> make sure it is not null and saved in database
        BrandName updatedBrandDB = brandNameRepo.findByName(updatedBrand);
        assertNotNull(updatedBrandDB);
        //then -> make sure that we override previous brandDB, with new name
        assertThat(updatedBrandDB.getId()).isEqualTo(brandDB.getId());
        assertThat(updatedBrandDB.getName()).isEqualTo(updatedBrand);

        //######### test case when updating is failed #############

        //given -> brand name which does not exist in db
        String brandNotFound = "notExistsBrand";
        BrandNameRepo brandNameRepoMock = mock(BrandNameRepo.class);
        BrandServiceImpl brandServiceMock = spy(new BrandServiceImpl(brandNameRepoMock));
        // when -> repository returns null on not found name
        given(brandNameRepoMock.findByName(brandNotFound)).willReturn(null);
        //then -> throws custom error, saying that brand not found and never updated
        assertThrows(CustomItemsException.class,()-> brandServiceMock.updateBrand(brandNotFound,new BrandName("anyName","500")));
        verify(brandNameRepoMock, never()).save(any(BrandName.class));
    }

    @Order(3)
    @Test
    @DisplayName("delete Brand and throw exception when brand is not found by id")
    void brandDeletedTest(){
        //given
        String removedBrand = "brandToRemove";
        //when -> save new brand and remove it from DB
        brandService.postNewBrandName(new BrandName(removedBrand, "100"));
        brandService.brandDeleted(brandNameRepo.findByName(removedBrand).getId());
        //then -> verify that it returns null when we try to search deleted brand from DB again
        assertNull(brandNameRepo.findByName(removedBrand));

        //######### test case when removal is failed #############

        BrandNameRepo brandNameRepoMock = mock(BrandNameRepo.class);
        BrandServiceImpl brandServiceMock = spy(new BrandServiceImpl(brandNameRepoMock));
        when(brandNameRepoMock.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(CustomItemsException.class,()->brandServiceMock.brandDeleted(anyLong()));
        verify(brandNameRepoMock, never()).deleteById(anyLong());
    }
}

