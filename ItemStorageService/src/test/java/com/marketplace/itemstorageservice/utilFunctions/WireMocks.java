package com.marketplace.itemstorageservice.utilFunctions;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.http.entity.ContentType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

public class WireMocks {

    public static void wireMockServer_GET_With_OK_ResourceBody(WireMockServer wireMockServer, String resourceFile, String uri){
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(uri))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type",ContentType.DEFAULT_TEXT.toString())
                        .withBodyFile(resourceFile)));
    }

    public static void wireMockServer_GET_With_OK_ReturnMessage(WireMockServer wireMockServer, String returnMessage, String uri){
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(uri))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type",ContentType.DEFAULT_TEXT.toString())
                        .withBodyFile(returnMessage)));
    }

    public static void wireMockServer_POST_With_OK(WireMockServer wireMockServer, String returnMessage, String uri){
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(uri))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type",ContentType.DEFAULT_TEXT.toString())
                        .withBody(returnMessage)));
    }

    public static void wireMockServer_POST_With_BAD_REQUEST(WireMockServer wireMockServer, String resourceFile, String uri){
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(uri))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(resourceFile)));
    }

    public static void wireMockServer_GET_With_BAD_REQUEST(WireMockServer wireMockServer, String resourceFile, String uri){
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(uri))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(resourceFile)));
    }

    public static void webTestClient_GET_With_OK_ResponseBody(WebTestClient client, String uri){
        client.get().uri(uri)
                .exchange().expectStatus().isOk()
                .expectBody();
    }

    public static void webTestClient_GET_With_OK_JsonBody(WebTestClient client, String uri, String serialized){
        client.get().uri(uri)
                .exchange().expectStatus().isOk()
                .expectBody().json(serialized);
    }

    public static void webTestClient_GET_With_BAD_REQUEST(WebTestClient client, String uri, String errorStatus){
        client.get().uri(uri)
                .accept(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                .exchange().expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(errorStatus);
    }

    public static String webTestClient_POST_With_OK_ReturnMessage(WebTestClient client, String requestBody, String uri){
        return client.post().uri(uri)
                .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                .accept(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                .body(BodyInserters.fromValue(requestBody))
                .exchange().expectStatus().isOk()
                .expectBody()
                .returnResult().toString();
    }

    public static void webTestClient_POST_With_BAD_REQUEST(WebTestClient client, String requestBody, String uri, String errorStatus){
        client.post().uri(uri)
                .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                .accept(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                .body(BodyInserters.fromValue(requestBody))
                .exchange().expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(errorStatus);
    }

}
