package edu.clubhouseapi.controller;

import static edu.clubhouseapi.common.TestAuthConst.COOKIE;
import static edu.clubhouseapi.common.TestAuthConst.PHONE_NUMBER;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

import edu.clubhouseapi.util.ByteUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockserver.matchers.MatchType;
import org.mockserver.model.Header;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.List;

public class CommonControllerTest extends BaseMockTest {

    @Autowired
    protected ByteUtils byteUtils;

    @Test
    public void testUnknownEndpoint() {
        testClientBuilder.build()
                .post()
                .uri("/api/unknown")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .bodyValue("{\"phone_number\": \"+89606666666\"}")
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectHeader()
                .valueEquals("Content-Type", "application/json")
                .expectBody()
                .jsonPath("success")
                .isEqualTo("false");

    }

    @Test
    public void testWrongBody() {
        testClientBuilder.build()
                .post()
                .uri("/api/start_phone_number_auth")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .bodyValue("{\"phone_number2\": \"+89606666666\"}")
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectHeader()
                .valueEquals("Content-Type", "application/json")
                .expectBody()
                .jsonPath("success")
                .isEqualTo("false")
                .jsonPath("error_message")
                .isEqualTo("400 Bad Request");

    }

    @Test
    public void testWrongAuth() {
        String cookie = COOKIE;
        Mockito.when(byteUtils.randomCookieId()).thenReturn(cookie);

        List<Header> headers = getBasicHeaders();
        headers.add(new Header("CH-UserID", "(null)"));
        headers.add(new Header("CH-DeviceId", byteUtils.fixedUuidFromString("__cfduid=" + cookie)));
        headers.add(new Header("Cookie", "__cfduid=" + cookie));
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(headers)
                        .withBody(json("{" + " \"phone_number\": \"" + PHONE_NUMBER + "\"}",
                                MatchType.ONLY_MATCHING_FIELDS))
                        .withPath("/start_phone_number_auth"))
                .respond(response().withStatusCode(HttpStatus.UNAUTHORIZED.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": false, \"unused_attr\": 2}"));

        testClientBuilder.build()
                .post()
                .uri("/api/start_phone_number_auth")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .bodyValue("{\"phone_number\": \"" + PHONE_NUMBER + "\"}")
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectHeader()
                .valueEquals("Content-Type", "application/json")
                .expectBody()
                .jsonPath("success")
                .isEqualTo("false");

    }

}
