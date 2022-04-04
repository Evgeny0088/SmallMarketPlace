package com.marketplace.itemstorageservice.configs;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

public class RedisCustomContainer extends GenericContainer<RedisCustomContainer>{

    private static final Logger logger = LoggerFactory.getLogger(RedisCustomContainer.class);

    private static final int REDIS_PORT = 6379;
    private static final String IMAGE_VERSION = "redis:latest";
    private static final String PASSWORD = "Primera77!";
    private static final String COMMAND = String.format("redis-server --requirepass %s",PASSWORD);
    private static RedisCustomContainer container;

    private RedisCustomContainer(){
        super(IMAGE_VERSION);
    }

    public static RedisCustomContainer getInstance() {
        if (container == null) {
            container = new RedisCustomContainer()
                    .withExposedPorts(REDIS_PORT)
                    .withCommand(COMMAND);
            container.start();
            logger.info("redis container is started!>>>>");
        }
        return container;
    }

    public int getRedisPort(){
        return REDIS_PORT;
    }

}
