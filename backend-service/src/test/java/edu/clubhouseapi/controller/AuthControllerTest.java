package edu.clubhouseapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

import edu.clubhouseapi.common.TestAuthConst;
import edu.clubhouseapi.service.TestLoginService;
import edu.clubhouseapi.util.ByteUtils;
import edu.clubhouseapi.util.ClassPathUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockserver.matchers.MatchType;
import org.mockserver.model.Header;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class AuthControllerTest extends BaseMockTest {

    @Autowired
    private TestAuthorizedController controller;

    @Autowired
    protected ByteUtils byteUtils;

    @Autowired
    TestLoginService testLoginService;

    @Autowired
    ClassPathUtils classPathUtils;

    public static final String COOKIE = TestAuthConst.COOKIE;

    public static final String USER_ID = TestAuthConst.USER_ID;

    public static final String TOKEN_PREFIX = TestAuthConst.TOKEN_PREFIX;

    public static final String TOKEN = TestAuthConst.TOKEN;

    public static final String PHONE_NUMBER = TestAuthConst.PHONE_NUMBER;

    public static final String VERIFICATION_CODE = TestAuthConst.VERIFICATION_CODE;

    @Test
    public void testStartPhoneAuthOk() {
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
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2}"));

        testClientBuilder.build()
                .post()
                .uri("/api/start_phone_number_auth")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .bodyValue("{\"phone_number\": \"" + PHONE_NUMBER + "\"}")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectHeader()
                .valueEquals("Content-Type", "application/json")
                .expectBody()
                .jsonPath("success")
                .isEqualTo("true")
                .jsonPath("cookie")
                .isEqualTo("__cfduid=" + cookie);

        mockServer.verify(
                request().withMethod(HttpMethod.POST.name()).withPath("/start_phone_number_auth").withHeaders(headers));
    }

    @Test
    public void testStartPhoneAuthWrongVersionNotOk() {
        String cookie = COOKIE;
        Mockito.when(byteUtils.randomCookieId()).thenReturn(cookie);

        List<Header> headers = getBasicHeaders();
        headers.add(new Header("CH-UserID", "(null)"));
        headers.add(new Header("CH-DeviceId", byteUtils.fixedUuidFromString("__cfduid=" + cookie)));
        headers.add(new Header("Cookie", "__cfduid=" + cookie));

        headers.remove(new Header("CH-AppVersion", "0.1.28"));
        headers.add(new Header("CH-AppVersion", "WRONG"));

        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(headers)
                        .withPath("/start_phone_number_auth")
                        .withBody(json("{" + " \"phone_number\": \"" + PHONE_NUMBER + "\"}",
                                MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true}"));

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

    @Test
    public void completePhoneAuthOk() {
        String cookie = "__cfduid=" + COOKIE;
        Mockito.when(byteUtils.randomCookieId()).thenReturn(cookie.replace("__cfduid=", ""));

        addWaitListedMockEndpoint(cookie, false);

        List<Header> headers = getBasicHeaders();
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
                                + "\", \"is_waitlisted\": false,  \"is_onboarding\": false,"
                                + " \"user_profile\" : {\"user_id\": \""
                                + USER_ID
                                + "\", \"unused_attr\": 2, \"username\": \"test\"}"
                                + "}"));

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
                .isEqualTo("false")
                .jsonPath("is_onboarding")
                .isEqualTo("false")
                .jsonPath("user_id")
                .isEqualTo(USER_ID);

        mockServer.verify(request().withMethod(HttpMethod.POST.name())
                .withPath("/complete_phone_number_auth")
                .withHeaders(headers));
    }

    @Test
    public void completePhoneAuthOkTestLogin() {
        String cookie = "__cfduid=" + COOKIE;
        Mockito.when(byteUtils.randomCookieId()).thenReturn(cookie.replace("__cfduid=", ""));

        addWaitListedMockEndpoint(cookie, false);

        List<Header> headers = getBasicHeaders();
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
                                + "\", \"is_waitlisted\": false,  \"is_onboarding\": false,"
                                + " \"user_profile\" : {\"user_id\": \""
                                + USER_ID
                                + "\", \"unused_attr\": 2, \"username\": \"test\"}"
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
                .isEqualTo("false")
                .jsonPath("is_onboarding")
                .isEqualTo("false")
                .jsonPath("user_id")
                .isEqualTo(USER_ID)
                .jsonPath("user_device")
                .isEqualTo(byteUtils.fixedUuidFromString(cookie))
                .jsonPath("token")
                .value(t -> innerToken.set((String) t));

        testClientBuilder.build()
                .post()
                .uri("/api/test_auth")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + innerToken.get())
                .bodyValue("{\"test\": \"test\"}")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("test")
                .isEqualTo("test");
    }

    @Test
    public void completePhoneAuthOkTestLoginStillWaitingError() {
        String cookie = "__cfduid=" + COOKIE;
        Mockito.when(byteUtils.randomCookieId()).thenReturn(cookie.replace("__cfduid=", ""));

        addWaitListedMockEndpoint(cookie, true);

        List<Header> headers = getBasicHeaders();
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
                                + "\", \"is_waitlisted\": true,  \"is_onboarding\": false,"
                                + " \"user_profile\" : {\"user_id\": \""
                                + USER_ID
                                + "\", \"unused_attr\": 2, \"username\": \"test\"}"
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
                .isEqualTo("true")
                .jsonPath("is_onboarding")
                .isEqualTo("false")
                .jsonPath("user_id")
                .isEqualTo(USER_ID)
                .jsonPath("token")
                .value(t -> innerToken.set((String) t));

        testClientBuilder.build()
                .post()
                .uri("/api/test_auth")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + innerToken.get())
                .bodyValue("{\"test\": \"test\"}")
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    public void testRemoteLogin() {
        String cookie = "__cfduid=" + COOKIE;
        Mockito.when(byteUtils.randomCookieId()).thenReturn(cookie.replace("__cfduid=", ""));

        addWaitListedMockEndpoint(cookie, false);

        List<Header> headers = getBasicHeaders();
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
                                + "\", \"is_waitlisted\": false,  \"is_onboarding\": false,"
                                + " \"user_profile\" : {\"user_id\": \""
                                + USER_ID
                                + "\", \"unused_attr\": 2, \"username\": \"test\"}"
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
                .isEqualTo("false")
                .jsonPath("is_onboarding")
                .isEqualTo("false")
                .jsonPath("user_id")
                .isEqualTo(USER_ID)
                .jsonPath("token")
                .value(t -> innerToken.set((String) t));

        headers.remove(new Header("CH-UserID", "(null)"));
        headers.add(new Header("CH-UserID", USER_ID));
        headers.add(new Header("CH-DeviceId", byteUtils.fixedUuidFromString(cookie)));
        headers.add(new Header("Cookie", cookie));
        headers.add(new Header("Authorization", TOKEN_PREFIX + TOKEN));
        mockServer.when(request().withMethod(HttpMethod.POST.name()).withHeaders(headers).withPath("/test_remote_auth"))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"test\": \"test2\"}"));

        testClientBuilder.build()
                .post()
                .uri("/api/test_remote_auth")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + innerToken.get())
                .bodyValue("{\"test\": \"test\"}")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("test")
                .isEqualTo("test2");
    }

    @Test
    public void testUserInfo() {
        String token = login(false);
        String cookie = "__cfduid=" + COOKIE;
        AtomicReference<List<String>> roles = new AtomicReference<>();
        testClientBuilder.build()
                .get()
                .uri("/api/user_info")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("user_id")
                .isEqualTo(USER_ID)
                .jsonPath("user_cookie")
                .isEqualTo(cookie)
                .jsonPath("user_device")
                .isEqualTo(byteUtils.fixedUuidFromString(cookie))
                .jsonPath("user_token")
                .isEqualTo(TOKEN)
                .jsonPath("roles")
                .value(t -> roles.set((List<String>) t));
        assertThat(roles.get()).contains("ROLE_ACTIVE");

    }

    @Test
    public void testWaitListedFalse() throws IOException {
        String token = login(false);
        String cookie = "__cfduid=" + COOKIE;
        List<Header> headers = getBasicHeaders();
        headers.add(new Header("Cookie", cookie));
        headers.add(new Header("CH-UserID", USER_ID));
        headers.add(new Header("CH-DeviceId", byteUtils.fixedUuidFromString(cookie)));
        headers.add(new Header("Authorization", TOKEN_PREFIX + TOKEN));
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(headers)
                        .withPath("/check_waitlist_status")
                        .withBody(""))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2, \"is_waitlisted\": false }"));

        // /me
        mockServer.when(request().withMethod(HttpMethod.POST.name())
                .withHeaders(testLoginService.getRequiredLoginHeaders())
                .withPath("/me")
                .withBody(json("{"
                                + " \"return_blocked_ids\": true, \"timezone_identifier\": \"Asia/Tokyo\", \"return_following_ids\": true}",
                        MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(classPathUtils.getResourceAsString("responses/me.json",
                                StandardCharsets.UTF_8)));

        AtomicReference<String> newToken = new AtomicReference<>();
        testClientBuilder.build()
                .post()
                .uri("/api/check_waitlist_status")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .bodyValue("")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectHeader()
                .valueEquals("Content-Type", "application/json")
                .expectBody()
                .jsonPath("success")
                .isEqualTo("true")
                .jsonPath("is_waitlisted")
                .isEqualTo(false)
                .jsonPath("token")
                .value(t -> newToken.set((String) t));

        // check waitlisted status for new token
        AtomicReference<List<String>> roles = new AtomicReference<>();
        testClientBuilder.build()
                .get()
                .uri("/api/user_info")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + newToken.get())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("user_id")
                .isEqualTo(USER_ID)
                .jsonPath("user_cookie")
                .isEqualTo(cookie)
                .jsonPath("user_device")
                .isEqualTo(byteUtils.fixedUuidFromString(cookie))
                .jsonPath("user_token")
                .isEqualTo(TOKEN)
                .jsonPath("roles")
                .value(t -> roles.set((List<String>) t));
        assertThat(roles.get()).contains("ROLE_ACTIVE");
        assertThat(roles.get()).contains("ROLE_NOWAIT");
    }

    @Test
    public void testWaitListedTrue() throws IOException {
        String token = login(true);
        String cookie = "__cfduid=" + COOKIE;
        List<Header> headers = getBasicHeaders();
        headers.add(new Header("Cookie", cookie));
        headers.add(new Header("CH-UserID", USER_ID));
        headers.add(new Header("CH-DeviceId", byteUtils.fixedUuidFromString(cookie)));
        headers.add(new Header("Authorization", TOKEN_PREFIX + TOKEN));
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(headers)
                        .withPath("/check_waitlist_status")
                        .withBody(""))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2, \"is_waitlisted\": true }"));

        // /me
        mockServer.when(request().withMethod(HttpMethod.POST.name())
                .withHeaders(testLoginService.getRequiredLoginHeaders())
                .withPath("/me")
                .withBody(json("{"
                                + " \"return_blocked_ids\": true, \"timezone_identifier\": \"Asia/Tokyo\", \"return_following_ids\": true}",
                        MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(classPathUtils.getResourceAsString("responses/me.json",
                                StandardCharsets.UTF_8)));

        AtomicReference<String> newToken = new AtomicReference<>();
        testClientBuilder.build()
                .post()
                .uri("/api/check_waitlist_status")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .bodyValue("")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectHeader()
                .valueEquals("Content-Type", "application/json")
                .expectBody()
                .jsonPath("success")
                .isEqualTo("true")
                .jsonPath("is_waitlisted")
                .isEqualTo(true)
                .jsonPath("token")
                .value(t -> newToken.set((String) t));

        // check waitlisted status for new token
        AtomicReference<List<String>> roles = new AtomicReference<>();
        testClientBuilder.build()
                .get()
                .uri("/api/user_info")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + newToken.get())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("user_id")
                .isEqualTo(USER_ID)
                .jsonPath("user_cookie")
                .isEqualTo(cookie)
                .jsonPath("user_device")
                .isEqualTo(byteUtils.fixedUuidFromString(cookie))
                .jsonPath("user_token")
                .isEqualTo(TOKEN)
                .jsonPath("roles")
                .value(t -> roles.set((List<String>) t));
        assertThat(roles.get()).doesNotContain("ROLE_ACTIVE");
        assertThat(roles.get()).doesNotContain("ROLE_NOWAIT");
    }

    @Test
    public void testAlreadyAuthenticatedAuthError() {
        String token = login(false);

        testClientBuilder.build()
                .post()
                .uri("/api/start_phone_number_auth")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .bodyValue("{\"phone_number\": \"" + PHONE_NUMBER + "\"}")
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectHeader()
                .valueEquals("Content-Type", "application/json")
                .expectBody()
                .jsonPath("success")
                .isEqualTo("false");

    }

    protected void addWaitListedMockEndpoint(String cookie, boolean waitListed) {
        List<Header> loggedInheaders = getBasicHeaders();
        loggedInheaders.add(new Header("Cookie", cookie));
        loggedInheaders.add(new Header("CH-UserID", USER_ID));
        loggedInheaders.add(new Header("CH-DeviceId", byteUtils.fixedUuidFromString(cookie)));
        loggedInheaders.add(new Header("Authorization", TOKEN_PREFIX + TOKEN));
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(loggedInheaders)
                        .withPath("/check_waitlist_status")
                        .withBody(""))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2, \"is_waitlisted\": " + waitListed + " }"));
    }


    public String login(boolean waitListed) {
        return testLoginService.login(waitListed, mockServer, testClientBuilder);
    }

}
