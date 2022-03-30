package com.marketplace.itemstorageservice.utilFunctions;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

public class ItemRequestBodyJsonData implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Stream.of(
                Arguments.of("100", "1", "2", "ITEM", """
                        {"serial":100,"brandName":{"id":1},"parentItem":{"id":2},"item_type":"ITEM"}"""),
                Arguments.of("null", "1", "2", "HKJHK", """
                        {"serial":null,"brandName":{"id":1},"parentItem":{"id":2},"item_type":""}"""),
                Arguments.of("null", "null", "2", "PACK", """
                        {"serial":null,"brandName":null,"parentItem":{"id":2},"item_type":"PACK"}"""),
                Arguments.of("100", "null", "2", " ", """
                        {"serial":100,"brandName":null,"parentItem":{"id":2},"item_type":""}"""),
                Arguments.of("null", "1", "2", "null", """
                        {"serial":null,"brandName":{"id":1},"parentItem":{"id":2},"item_type":""}"""),
                Arguments.of("100", "1", "2", "ITEM", """
                        {"serial":100,"brandName":{"id":1},"parentItem":{"id":2},"item_type":"ITEM"}"""),
                Arguments.of("null", "null", "null", " ", """
                        {"serial":null,"brandName":null,"parentItem":null,"item_type":""}"""),
                Arguments.of("null", "null", "2", "", """
                        {"serial":null,"brandName":null,"parentItem":{"id":2},"item_type":""}"""),
                Arguments.of("null", "null", "null", "null", """
                        {"serial":null,"brandName":null,"parentItem":null,"item_type":""}""")
        );
    }
}
