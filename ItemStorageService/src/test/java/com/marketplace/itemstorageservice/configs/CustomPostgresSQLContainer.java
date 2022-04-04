package com.marketplace.itemstorageservice.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;

public class CustomPostgresSQLContainer extends PostgreSQLContainer<CustomPostgresSQLContainer> {

    private static final Logger logger = LoggerFactory.getLogger(CustomPostgresSQLContainer.class);

    private static CustomPostgresSQLContainer container;
    private static final String DATA_BASE_NAME = "itemstoragetest";
    private static final String USERNAME = "evgeny88";
    private static final String PASSWORD = "Primera77!";

    private static final String IMAGE_VERSION = "postgres:13";

    private CustomPostgresSQLContainer() {
        super(IMAGE_VERSION);
    }

    public static CustomPostgresSQLContainer getInstance() {
        if (container == null) {
            container = new CustomPostgresSQLContainer()
                    .withDatabaseName(DATA_BASE_NAME)
                    .withUsername(USERNAME)
                    .withPassword(PASSWORD);
            container.start();
            logger.info("postgres container is started!>>>>");
        }
        return container;
    }
}
