package com.smallmarketplace;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisplayName("check if request body for item creation is correct with any inputs")
public class ItemRequestBodyJsonTest {

    private static final Logger logger = LoggerFactory.getLogger(ItemRequestBodyJsonTest.class);

    @ParameterizedTest(name = "test case: => serial={0}, brandId={1}, parentId={2}, item_type={3}, expectedBody={4}")
    @ArgumentsSource(ItemRequestBodyJsonData.class)
    void itemRequestBodyJsonTest(String serial, String brandId, String parentId, String item_type, String expectedBody){
        String actual = RequestBodyParser.requestBody(serial, brandId, parentId, item_type);
        logger.info("request body results:");
        Assertions.assertEquals(expectedBody, actual);
    }
}
