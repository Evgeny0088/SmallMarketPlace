package com.marketplace.itemstorageservice.utilFunctions;

import com.marketplace.itemstorageservice.models.ItemType;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.springframework.boot.test.context.TestConfiguration;

import java.util.stream.Stream;

@TestConfiguration
public class ItemUpdateValidArguments implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Stream.of(
                Arguments.of(3L, 100L, "gucci", -1L, ItemType.PACK),
                Arguments.of(1L, 100L, "cc", 2L, ItemType.ITEM),
                Arguments.of(4L, 100L, "cc", 2L, ItemType.PACK),
                Arguments.of(7L, 100L, "cc", 2L, ItemType.PACK),
                Arguments.of(7L, 100L, "gucci", 1L, ItemType.ITEM)
        );
    }
}
