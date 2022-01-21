package com.marketPlace.Orders.DTOModels;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class ItemDetailedInfoDTO {
    private final long itemPackId;
    private final long serial;
    private final String brandName;
    private final String brandVersion;
    private long itemCountInPack;

    public ItemDetailedInfoDTO(long itemPackId, long serial, String brandName, String brandVersion, long itemCountInPack) {
        this.itemPackId = itemPackId;
        this.serial = serial;
        this.brandName = brandName;
        this.brandVersion = brandVersion;
        this.itemCountInPack = itemCountInPack;
    }
}
