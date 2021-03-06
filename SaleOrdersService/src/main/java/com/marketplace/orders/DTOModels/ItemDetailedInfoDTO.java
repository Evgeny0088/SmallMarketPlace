package com.marketplace.orders.DTOModels;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@ToString
@Setter
@Getter
public class ItemDetailedInfoDTO implements Serializable {
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
