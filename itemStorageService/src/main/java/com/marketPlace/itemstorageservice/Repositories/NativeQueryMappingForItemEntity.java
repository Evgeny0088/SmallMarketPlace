package com.marketPlace.itemstorageservice.Repositories;

import com.marketPlace.itemstorageservice.DTOModels.ItemDetailedInfoDTO;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.List;

@Component
public class NativeQueryMappingForItemEntity {

    @PersistenceContext
    public EntityManager entityManager;

    public List<ItemDetailedInfoDTO> getItemsDTO(){
        TypedQuery<ItemDetailedInfoDTO> query = entityManager.createNamedQuery("findAllItemsDTO",ItemDetailedInfoDTO.class);
        return query.getResultList();
    }
}
