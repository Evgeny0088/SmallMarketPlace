package com.marketPlace.itemstorageservice.repositories;

import com.marketPlace.itemstorageservice.DTOmodels.ItemDetailedInfoDTO;
import com.marketPlace.itemstorageservice.models.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepo extends JpaRepository<Item, Long> {

    @Query(nativeQuery = true, name = "findAllItemsDTO")
    List<ItemDetailedInfoDTO> findAllItemsDTO();

    @Query(nativeQuery = true, name = "getItemDTOByParentId")
    ItemDetailedInfoDTO getItemDTOByParentId(@Param("parent_id") Long item_id);

    @Modifying
    @Query(nativeQuery = true,name = "removeItemsFromPackage")
    void removeItemsFromPackage(@Param("parent_id") Long parent_id, @Param("items_count") Long items_count);
}

