package com.marketPlace.itemstorageservice.repositories;

import com.marketPlace.itemstorageservice.DTOModels.ItemDetailedInfoDTO;
import com.marketPlace.itemstorageservice.models.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepo extends JpaRepository<Item, Long> {

    @Query(nativeQuery = true, name = "findAllItemsDTO")
    List<ItemDetailedInfoDTO> findAllItemsDTO();

    @Query(nativeQuery = true, name = "getItemDTOByParentId")
    ItemDetailedInfoDTO getItemDTOByParentId(@Param("parent_id") Long item_id);

}

