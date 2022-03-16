package com.marketplace.itemstorageservice.DTOmodels;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class ItemDetailedInfoDTO {
    private final long itemPackageId;
    private final long serial;
    private final String brandName;
    private final String brandVersion;
    private long itemsQuantityInPack;

    public ItemDetailedInfoDTO(long itemPackageId, long serial, long itemsQuantityInPack, String brandName, String brandVersion) {
        this.itemPackageId = itemPackageId;
        this.serial = serial;
        this.itemsQuantityInPack = itemsQuantityInPack;
        this.brandName = brandName;
        this.brandVersion = brandVersion;
    }
}
