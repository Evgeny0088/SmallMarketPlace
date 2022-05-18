package com.marketplace.itemstorageservice.configs;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public abstract class WireMockConfig {

    public static WireMockServer getWireMockServer(){
        return new WireMockServer(options().dynamicPort().dynamicHttpsPort());
    }

    @BeforeAll
    public static void init(){
        getWireMockServer().start();
    }

    @BeforeEach
    void clearWireMock() {
        getWireMockServer().resetAll();
    }

    @AfterAll
    public static void destroy(){
        getWireMockServer().stop();
    }

}
