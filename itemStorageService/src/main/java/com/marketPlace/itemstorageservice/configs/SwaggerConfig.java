package com.marketPlace.itemstorageservice.configs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
//@OpenAPIDefinition
public class SwaggerConfig {

//    @Bean
//    public GroupedOpenApi publicApi(@Value("${server.servlet.context-path}/api/v1") String contextPath) {
//        return GroupedOpenApi.builder()
//                .group("brandController")
//                .pathsToMatch(String.format("%s/brands/**", contextPath))
//                .build();
//    }

//    @Bean
//    public GroupedOpenApi itemControllerApi(@Value("${server.servlet.context-path}") String contextPath) {
//        return GroupedOpenApi.builder()
//                .group("itemController")
//                .pathsToMatch(String.format("%s/items/**", contextPath))
//                .build();
//    }
//
//    @Bean
//    public GroupedOpenApi ItemDTOControllerApi(@Value("${server.servlet.context-path}") String contextPath) {
//        return GroupedOpenApi.builder()
//                .group("itemDTOController")
//                .pathsToMatch(String.format("%s/itemsDTO/**",contextPath))
//                .build();
//    }

}
