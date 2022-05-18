package com.marketplace.itemstorageservice.utilFunctions;

import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

public class WireMockClientResponses {

    public static void webTestClient_GET_With_OK_ResponseBody(WebTestClient client, String uri){
        client.get().uri(uri)
                .exchange().expectStatus().isOk()
                .expectBody();
    }

    public static void webTestClient_GET_With_OK_JsonBody(WebTestClient client, String uri){
        client.get().uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectBody().returnResult();
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
                .accept(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON)
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
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(errorStatus);
    }
}
