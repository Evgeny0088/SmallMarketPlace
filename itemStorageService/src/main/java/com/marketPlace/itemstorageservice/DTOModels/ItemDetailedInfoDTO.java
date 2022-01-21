package com.marketPlace.itemstorageservice.DTOModels;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.SqlResultSetMapping;

@ToString
@Setter
@Getter
public class ItemDetailedInfoDTO {
    private final long itemPackageId;
    private final long serial;
    private long itemsQuantityInPack;
    private final String brandName;
    private final String brandVersion;

    public ItemDetailedInfoDTO(long itemPackageId, long serial, long itemsQuantityInPack, String brandName, String brandVersion) {
        this.itemPackageId = itemPackageId;
        this.serial = serial;
        this.itemsQuantityInPack = itemsQuantityInPack;
        this.brandName = brandName;
        this.brandVersion = brandVersion;
    }
}
