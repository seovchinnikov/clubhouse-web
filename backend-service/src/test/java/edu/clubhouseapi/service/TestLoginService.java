package edu.clubhouseapi.service;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

import edu.clubhouseapi.common.TestAuthConst;
import edu.clubhouseapi.util.ByteUtils;
import org.mockito.Mockito;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.MatchType;
import org.mockserver.model.Header;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class TestLoginService {

    public static final String COOKIE = TestAuthConst.COOKIE;

    public static final String USER_ID = TestAuthConst.USER_ID;

    public static final String TOKEN_PREFIX = TestAuthConst.TOKEN_PREFIX;

    public static final String TOKEN = TestAuthConst.TOKEN;

    public static final String PHONE_NUMBER = TestAuthConst.PHONE_NUMBER;

    public static final String VERIFICATION_CODE = TestAuthConst.VERIFICATION_CODE;

    @Autowired
    ByteUtils byteUtils;

    public String login(boolean waitListed, ClientAndServer mockServer, WebTestClient.Builder testClientBuilder) {
        String cookie = "__cfduid=" + COOKIE;
        Mockito.when(byteUtils.randomCookieId()).thenReturn(cookie.replace("__cfduid=", ""));

        List<Header> headers = TestAuthConst.getBasicHeaders();
        headers.add(new Header("Cookie", cookie));
        headers.add(new Header("CH-UserID", "(null)"));
        headers.add(new Header("CH-DeviceId", byteUtils.fixedUuidFromString(cookie)));
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(headers)
                        .withPath("/complete_phone_number_auth")
                        .withBody(json("{"
                                + " \"phone_number\": \""
                                + PHONE_NUMBER
                                + "\", "
                                + " \"verification_code\": \""
                                + VERIFICATION_CODE
                                + "\"}", MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2, \"auth_token\": \""
                                + TOKEN
                                + "\", \"is_waitlisted\": "
                                + waitListed
                                + ",  \"is_onboarding\": false,"
                                + " \"user_profile\" : {\"user_id\": \""
                                + USER_ID
                                + "\", \"unused_attr\": 2}"
                                + "}"));

        AtomicReference<String> innerToken = new AtomicReference<>();
        testClientBuilder.build()
                .post()
                .uri("/api/complete_phone_number_auth")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .bodyValue("{\"phone_number\": \""
                        + PHONE_NUMBER
                        + "\", \"verification_code\": \""
                        + VERIFICATION_CODE
                        + "\", \"cookie\": \""
                        + cookie
                        + "\"}")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectHeader()
                .valueEquals("Content-Type", "application/json")
                .expectBody()
                .jsonPath("success")
                .isEqualTo("true")
                .jsonPath("user_token")
                .isEqualTo(TOKEN)
                .jsonPath("is_waitlisted")
                .isEqualTo(waitListed)
                .jsonPath("is_onboarding")
                .isEqualTo("false")
                .jsonPath("user_id")
                .isEqualTo(USER_ID)
                .jsonPath("token")
                .value(t -> innerToken.set((String) t));

        return innerToken.get();
    }

    public List<Header> getRequiredLoginHeaders() {
        String cookie = "__cfduid=" + COOKIE;
        List<Header> headers = new ArrayList<>(TestAuthConst.getBasicHeaders());
        headers.add(new Header("Cookie", cookie));
        headers.add(new Header("CH-UserID", USER_ID));
        headers.add(new Header("CH-DeviceId", byteUtils.fixedUuidFromString(cookie)));
        headers.add(new Header("Authorization", TOKEN_PREFIX + TOKEN));
        return headers;
    }
}
