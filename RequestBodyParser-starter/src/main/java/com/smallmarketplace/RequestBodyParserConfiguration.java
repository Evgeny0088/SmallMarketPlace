package com.smallmarketplace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(RequestBodyParserConfiguration.class)
public class RequestBodyParserConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(RequestBodyParserConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public RequestBodyParser requestBodyParser() {
        logger.info("<<< requestBody parser is initiated! >>>");
        return new RequestBodyParser();
    }
}
