package edu.clubhouseapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.clubhouseapi.dto.GetClubResponse;
import edu.clubhouseapi.service.TestLoginService;
import edu.clubhouseapi.util.ClassPathUtils;
import org.junit.jupiter.api.Test;
import org.mockserver.matchers.MatchType;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ClubsControllerTest extends BaseMockTest {

    @Autowired
    TestLoginService testLoginService;

    @Autowired
    ClassPathUtils classPathUtils;

    @Test
    public void testGetClubOk() throws IOException {
        String token = testLoginService.login(false, mockServer, testClientBuilder);

        // get_club
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(testLoginService.getRequiredLoginHeaders())
                        .withPath("/get_club")
                        .withBody(json("{" + " \"club_id\": 4}", MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(classPathUtils.getResourceAsString("responses/get_club.json",
                                StandardCharsets.UTF_8)));

        byte[] responseBody = testClientBuilder.build()
                .post()
                .uri("/api/get_club")
                .bodyValue("{" + " \"club_id\": 4}")
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

        GetClubResponse clubResponse =
                new ObjectMapper().readValue(new String(responseBody, StandardCharsets.UTF_8), GetClubResponse.class);
        assertThat(clubResponse.getClubInfo().getName()).isEqualTo("test");

    }
}
