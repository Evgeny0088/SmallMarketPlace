package com.marketplace.itemstorageservice.services;

import com.marketplace.itemstorageservice.configs.ServiceTestConfig;
import com.marketplace.itemstorageservice.exceptions.CustomItemsException;
import com.marketplace.itemstorageservice.models.BrandName;
import com.marketplace.itemstorageservice.repositories.BrandNameRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

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

    @Test
    @DisplayName("post brands and get all brands from database")
    void getAllBrands(){
        //given
        brandsPersist();
        //when
        List<BrandName> allBrands = brandService.allBrands();
        //then
        assertEquals(3, allBrands.size());
    }

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

        //######### test case when saving is failed #############
        BrandNameRepo brandNameRepoMock = mock(BrandNameRepo.class);
        BrandServiceImpl brandServiceMock = spy(new BrandServiceImpl(brandNameRepoMock));
        //when
        given(brandNameRepoMock.findByName(brandName)).willReturn(brand);
        //then
        assertThrows(CustomItemsException.class,()-> brandServiceMock.postNewBrandName(brand));
        verify(brandNameRepoMock, never()).save(any(BrandName.class));
    }

    @DisplayName("update brand if exists and if request inputs are correct")
    @ParameterizedTest(name = "test case: => brand={0}")
    @CsvSource({"brandDBExisted, updatedBrand"})
    void updateBrandTest(String brandFromDB, String updatedBrand) {
        //given -> we persist brand in database
        brandService.postNewBrandName(new BrandName("brandDBExisted", "100"));
        BrandName brandDB = brandNameRepo.findByName(brandFromDB);
        //then it is saved in database and exists
        assertNotNull(brandDB);
        //then -> update all params and save it again in DB
        brandDB.setName(updatedBrand);
        brandDB.setVersion("200");
        brandDB.setCreateDate(LocalDateTime.now());
        brandNameRepo.save(brandDB);
        //given -> get updated brand from database and make sure that is not null
        BrandName updatedBrandDB = brandNameRepo.findByName(updatedBrand);
        //then -> make sure it is not null and saved in database
        // name is updated and id is kept the same as before since we are updating database object
        assertNotNull(updatedBrandDB);
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

    @Test
    @DisplayName("delete Brand and throws exception when brand is not found by id")
    void brandDeletedTest(){
        //given
        String removedBrand = "brandToRemove";
        //when -> saving new brand and remove it from DB
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

    private void brandsPersist(){
        int[] range = new int[]{0,1,2};
        IntStream.of(range).forEach(i->brandService.postNewBrandName(new BrandName(String.format("brand%s",i),"100")));
    }

}

