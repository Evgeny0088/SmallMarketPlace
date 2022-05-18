package com.marketplace.itemstorageservice.utilFunctions;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.http.entity.ContentType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class WireMockServers {

    public static void wireMockServer_GET_With_OK_ResourceBody(WireMockServer wireMockServer, String resourceFile, String uri){
        wireMockServer.stubFor(get(urlEqualTo(uri))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type",ContentType.APPLICATION_JSON.toString())
                        .withBodyFile(resourceFile)));
    }

    public static void wireMockServer_GET_With_OK_ReturnMessage(WireMockServer wireMockServer, String returnMessage, String uri){
        wireMockServer.stubFor(get(urlEqualTo(uri))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type",ContentType.DEFAULT_TEXT.toString())
                        .withBodyFile(returnMessage)));
    }

    public static void wireMockServer_POST_With_OK_ReturnMessage(WireMockServer wireMockServer, String returnMessage, String uri){
        wireMockServer.stubFor(post(urlEqualTo(uri))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type",ContentType.DEFAULT_TEXT.toString())
                        .withBody(returnMessage)));
    }

    public static void wireMockServer_POST_With_BAD_REQUEST(WireMockServer wireMockServer, String resourceFile, String uri){
        wireMockServer.stubFor(post(urlEqualTo(uri))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(resourceFile)));
    }

    public static void wireMockServer_GET_With_BAD_REQUEST(WireMockServer wireMockServer, String resourceFile, String uri){
        wireMockServer.stubFor(get(urlEqualTo(uri))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(resourceFile)));
    }
}
