package com.marketplace.itemstorageservice.utilFunctions;

import com.marketplace.itemstorageservice.models.ItemType;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.springframework.boot.test.context.TestConfiguration;

import java.util.stream.Stream;

@TestConfiguration
public class ItemCreationInValidArguments implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Stream.of(
                Arguments.of(100L, "gucci", -1L, ItemType.ITEM),
                Arguments.of(100L, "gucci", 2L, ItemType.ITEM),
                Arguments.of(100L, "gucci", 3L, ItemType.ITEM),
                Arguments.of(100L, "not valid brand", 1L, ItemType.PACK)
        );
    }
}
