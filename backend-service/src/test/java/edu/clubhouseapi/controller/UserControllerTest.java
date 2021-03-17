package edu.clubhouseapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.clubhouseapi.dto.FollowingResponse;
import edu.clubhouseapi.dto.MeResponse;
import edu.clubhouseapi.dto.NotificationsResponse;
import edu.clubhouseapi.dto.ProfileResponse;
import edu.clubhouseapi.dto.SuggestedFollowsResponse;
import edu.clubhouseapi.dto.UserSearchResponse;
import edu.clubhouseapi.service.TestLoginService;
import edu.clubhouseapi.util.ClassPathUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.mockserver.matchers.MatchType;
import org.mockserver.model.Header;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class UserControllerTest extends BaseMockTest {

    @Autowired
    TestLoginService testLoginService;

    @Autowired
    ClassPathUtils classPathUtils;

    @Test
    public void testGetProfileOk() throws IOException {
        String token = testLoginService.login(false, mockServer, testClientBuilder);

        // get_channels
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(testLoginService.getRequiredLoginHeaders())
                        .withPath("/get_profile"))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(classPathUtils.getResourceAsString("responses/get_profile.json",
                                StandardCharsets.UTF_8)));

        byte[] responseBody = testClientBuilder.build()
                .post()
                .uri("/api/get_profile")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .bodyValue("{\"user_id\": 2081027619}")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectHeader()
                .valueEquals("Content-Type", "application/json")
                .expectBody()
                .jsonPath("success")
                .isEqualTo("true")
                .returnResult()
                .getResponseBody();

        ProfileResponse profileResponse =
                new ObjectMapper().readValue(new String(responseBody, StandardCharsets.UTF_8), ProfileResponse.class);
        assertThat(profileResponse.getUserProfile().getName()).isEqualTo("suyoung Bin");
        assertThat(profileResponse.getUserProfile().getNumFollowers()).isEqualTo(135);

    }

    @Test
    public void testRecommendedFollowsOk() throws IOException {
        String token = testLoginService.login(false, mockServer, testClientBuilder);

        // get_suggested_follows_all
        mockServer
                .when(request().withMethod(HttpMethod.GET.name())
                        .withHeaders(testLoginService.getRequiredLoginHeaders())
                        .withPath("/get_suggested_follows_all")
                        .withQueryStringParameter("in_onboarding", "false")
                        .withQueryStringParameter("page_size", String.valueOf(50))
                        .withQueryStringParameter("page", String.valueOf(1)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(classPathUtils.getResourceAsString("responses/get_suggested_follows_all.json",
                                StandardCharsets.UTF_8), StandardCharsets.UTF_8)
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        byte[] responseBody = testClientBuilder.build()
                .get()
                .uri("/api/get_suggested_follows_all?page=1&page_size=50")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectHeader()
                .valueEquals("Content-Type", "application/json")
                .expectBody()
                .jsonPath("success")
                .isEqualTo("true")
                .returnResult()
                .getResponseBody();

        SuggestedFollowsResponse suggestedFollowsResponse = new ObjectMapper()
                .readValue(new String(responseBody, StandardCharsets.UTF_8), SuggestedFollowsResponse.class);
        assertThat(suggestedFollowsResponse.getCount()).isEqualTo(198);
        assertThat(suggestedFollowsResponse.getUsers().get(0).getName()).isEqualTo("Rohan Seth");

    }

    @Test
    public void testMeOk() throws IOException {
        String token = testLoginService.login(false, mockServer, testClientBuilder);

        // /me
        mockServer.when(request().withMethod(HttpMethod.POST.name())
                .withHeaders(testLoginService.getRequiredLoginHeaders())
                .withPath("/me")
                .withBody(json("{"
                        + " \"return_blocked_ids\": true, \"timezone_identifier\": \"Asia/Tokyo\", \"return_following_ids\": true}",
                        MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(classPathUtils.getResourceAsString("responses/me.json", StandardCharsets.UTF_8)));

        byte[] responseBody = testClientBuilder.build()
                .post()
                .uri("/api/me")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .bodyValue("{"
                        + " \"return_blocked_ids\": true, \"timezone_identifier\": \"Asia/Tokyo\", \"return_following_ids\": true}")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectHeader()
                .valueEquals("Content-Type", "application/json")
                .expectBody()
                .jsonPath("success")
                .isEqualTo("true")
                .returnResult()
                .getResponseBody();

        MeResponse profileResponse =
                new ObjectMapper().readValue(new String(responseBody, StandardCharsets.UTF_8), MeResponse.class);
        assertThat(profileResponse.getUserProfile().getName()).isEqualTo("Serhio");
        assertThat(profileResponse.getFollowingIds().size()).isEqualTo(5);

    }

    @Test
    public void testFollowOk() throws IOException {
        String token = testLoginService.login(false, mockServer, testClientBuilder);

        // /follow
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(testLoginService.getRequiredLoginHeaders())
                        .withPath("/follow")
                        .withBody(json("{" + " \"source\": 4}", MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2}"));

        byte[] responseBody = testClientBuilder.build()
                .post()
                .uri("/api/follow")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .bodyValue("{" + " \"user_id\": 444 }")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectHeader()
                .valueEquals("Content-Type", "application/json")
                .expectBody()
                .jsonPath("success")
                .isEqualTo("true")
                .returnResult()
                .getResponseBody();

    }

    @Test
    public void testUnFollowOk() throws IOException {
        String token = testLoginService.login(false, mockServer, testClientBuilder);

        // /follow
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(testLoginService.getRequiredLoginHeaders())
                        .withPath("/unfollow")
                        .withBody(json("{}", MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2}"));

        byte[] responseBody = testClientBuilder.build()
                .post()
                .uri("/api/unfollow")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .bodyValue("{" + " \"user_id\": 444 }")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectHeader()
                .valueEquals("Content-Type", "application/json")
                .expectBody()
                .jsonPath("success")
                .isEqualTo("true")
                .returnResult()
                .getResponseBody();

    }

    @Test
    public void testUserSearchOk() throws IOException {
        String token = testLoginService.login(false, mockServer, testClientBuilder);

        // /follow
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(testLoginService.getRequiredLoginHeaders())
                        .withPath("/search_users")
                        .withBody(json("{"
                                + " \"cofollows_only\": false,"
                                + " \"following_only\": false, \"followers_only\": false,"
                                + " \"query\": \"elon\"}", MatchType.ONLY_MATCHING_FIELDS)))
                .respond(
                        response().withStatusCode(HttpStatus.OK.value())
                                .withContentType(MediaType.APPLICATION_JSON)
                                .withBody(classPathUtils.getResourceAsString("responses/search_users.json",
                                        StandardCharsets.UTF_8), StandardCharsets.UTF_8)
                                .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        byte[] responseBody = testClientBuilder.build()
                .post()
                .uri("/api/search_users")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .bodyValue("{"
                        + " \"cofollows_only\": false,"
                        + " \"following_only\": false, \"followers_only\": false,"
                        + " \"query\": \"elon\", \"page_size\": 50, \"page\": 1}")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectHeader()
                .valueEquals("Content-Type", "application/json")
                .expectBody()
                .jsonPath("success")
                .isEqualTo("true")
                .returnResult()
                .getResponseBody();
        UserSearchResponse profileResponse = new ObjectMapper()
                .readValue(new String(responseBody, StandardCharsets.UTF_8), UserSearchResponse.class);
        assertThat(profileResponse.getUsers().get(0).getName()).isEqualTo("ELON ELON");

    }

    @Test
    public void testFollowingOk() throws IOException {
        String token = testLoginService.login(false, mockServer, testClientBuilder);

        mockServer
                .when(request().withMethod(HttpMethod.GET.name())
                        .withHeaders(testLoginService.getRequiredLoginHeaders())
                        .withPath("/get_following")
                        .withQueryStringParameter("page_size", String.valueOf(50))
                        .withQueryStringParameter("page", String.valueOf(1)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(classPathUtils.getResourceAsString("responses/get_following.json",
                                StandardCharsets.UTF_8), StandardCharsets.UTF_8)
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        byte[] responseBody = testClientBuilder.build()
                .get()
                .uri("/api/get_following?user_id=4&page=1&page_size=50")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectHeader()
                .valueEquals("Content-Type", "application/json")
                .expectBody()
                .jsonPath("success")
                .isEqualTo("true")
                .returnResult()
                .getResponseBody();

        FollowingResponse suggestedFollowsResponse = new ObjectMapper()
                .readValue(new String(responseBody, StandardCharsets.UTF_8), FollowingResponse.class);
        assertThat(suggestedFollowsResponse.getUsers().get(0).getName()).isEqualTo("Andrew Weiss");
        assertThat(suggestedFollowsResponse.getUsers().size()).isEqualTo(50);
    }

    @Test
    public void testFollowersOk() throws IOException {
        String token = testLoginService.login(false, mockServer, testClientBuilder);

        mockServer
                .when(request().withMethod(HttpMethod.GET.name())
                        .withHeaders(testLoginService.getRequiredLoginHeaders())
                        .withPath("/get_followers")
                        .withQueryStringParameter("page_size", String.valueOf(50))
                        .withQueryStringParameter("page", String.valueOf(1)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(classPathUtils.getResourceAsString("responses/get_followers.json",
                                StandardCharsets.UTF_8), StandardCharsets.UTF_8)
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        byte[] responseBody = testClientBuilder.build()
                .get()
                .uri("/api/get_followers?user_id=4&page=1&page_size=50")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectHeader()
                .valueEquals("Content-Type", "application/json")
                .expectBody()
                .jsonPath("success")
                .isEqualTo("true")
                .returnResult()
                .getResponseBody();

        FollowingResponse suggestedFollowsResponse = new ObjectMapper()
                .readValue(new String(responseBody, StandardCharsets.UTF_8), FollowingResponse.class);
        assertThat(suggestedFollowsResponse.getUsers().get(0).getName()).isEqualTo("Giorgio Fugardi");
        assertThat(suggestedFollowsResponse.getUsers().size()).isEqualTo(50);
    }

    @Test
    public void testNotificationsOk() throws IOException {
        String token = testLoginService.login(false, mockServer, testClientBuilder);

        mockServer
                .when(request().withMethod(HttpMethod.GET.name())
                        .withHeaders(testLoginService.getRequiredLoginHeaders())
                        .withPath("/get_notifications")
                        .withQueryStringParameter("page_size", String.valueOf(50))
                        .withQueryStringParameter("page", String.valueOf(1)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(classPathUtils.getResourceAsString("responses/get_notifications.json",
                                StandardCharsets.UTF_8), StandardCharsets.UTF_8)
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        byte[] responseBody = testClientBuilder.build()
                .get()
                .uri("/api/get_notifications?page=1&page_size=50")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectHeader()
                .valueEquals("Content-Type", "application/json")
                .expectBody()
                .jsonPath("success")
                .isEqualTo("true")
                .returnResult()
                .getResponseBody();

        NotificationsResponse suggestedFollowsResponse = new ObjectMapper()
                .readValue(new String(responseBody, StandardCharsets.UTF_8), NotificationsResponse.class);
        assertThat(suggestedFollowsResponse.getNotifications().get(0).getMessage()).isEqualTo("followed you");
        assertThat(suggestedFollowsResponse.getNotifications().get(0).getUserProfile().getName()).isEqualTo("Pavel DSDSD");
    }
}
