package edu.clubhouseapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.clubhouseapi.dto.ChannelsResponse;
import edu.clubhouseapi.dto.GetEventsResponse;
import edu.clubhouseapi.dto.JoinChannelResponse;
import edu.clubhouseapi.service.TestLoginService;
import edu.clubhouseapi.util.ClassPathUtils;
import org.junit.jupiter.api.Test;
import org.mockserver.matchers.MatchType;
import org.mockserver.model.Header;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ChannelsControllerTest extends BaseMockTest {

    @Autowired
    TestLoginService testLoginService;

    @Autowired
    ClassPathUtils classPathUtils;

    @Test
    public void testGetChannelsOk() throws IOException {
        String token = testLoginService.login(false, mockServer, testClientBuilder);

        // get_channels
        mockServer
                .when(request().withMethod(HttpMethod.GET.name())
                        .withHeaders(testLoginService.getRequiredLoginHeaders())
                        .withPath("/get_channels"))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(classPathUtils.getResourceAsString("responses/get_channels.json",
                                StandardCharsets.UTF_8)));

        byte[] responseBody = testClientBuilder.build()
                .get()
                .uri("/api/get_channels")
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

        ChannelsResponse channelsResponse =
                new ObjectMapper().readValue(new String(responseBody, StandardCharsets.UTF_8), ChannelsResponse.class);
        assertThat(channelsResponse.getChannels().size()).isEqualTo(2);

        assertThat(channelsResponse.getChannels().get(0)).satisfies(channelResponse -> {
            assertThat(channelResponse.getChannel()).isEqualTo("mWL2QdEP");
        });

        assertThat(channelsResponse.getChannels().get(0).getUsers().get(0)).satisfies(usersRespons -> {
            assertThat(usersRespons.getName()).isEqualTo("suyoung Bin");
        });

    }

    @Test
    public void testPingOk() throws IOException {
        String token = testLoginService.login(false, mockServer, testClientBuilder);

        // active_ping
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(testLoginService.getRequiredLoginHeaders())
                        .withPath("/active_ping")
                        .withBody(json("{" + " \"channel\": \"1234567\"}", MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2, \"should_leave\": false }")
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        testClientBuilder.build()
                .post()
                .uri("/api/active_ping")
                .bodyValue("{" + " \"channel\": \"1234567\"}")
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

    }

    @Test
    public void testJoinChannelOk() throws IOException {
        String token = testLoginService.login(false, mockServer, testClientBuilder);

        // join_channel
        mockServer.when(request().withMethod(HttpMethod.POST.name())
                .withHeaders(testLoginService.getRequiredLoginHeaders())
                .withBody(json("{"
                        + " \"channel\": \"1234567\", \"attribution_source\": \"feed\", \"attribution_details\": \"eyJpc19leHBsb3JlIjpmYWxzZSwicmFuayI6MX0=\"}",
                        MatchType.ONLY_MATCHING_FIELDS))
                .withPath("/join_channel"))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(classPathUtils.getResourceAsString("responses/join_channel.json",
                                StandardCharsets.UTF_8)));

        byte[] responseBody = testClientBuilder.build()
                .post()
                .uri("/api/join_channel")
                .bodyValue("{"
                        + " \"channel\": \"1234567\", \"attribution_source\": \"feed\", \"attribution_details\": \"eyJpc19leHBsb3JlIjpmYWxzZSwicmFuayI6MX0=\"}")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectHeader()
                .valueEquals("Content-Type", "application/json")
                .expectBody()
                .returnResult()
                .getResponseBody();

        JoinChannelResponse channelsResponse = new ObjectMapper()
                .readValue(new String(responseBody, StandardCharsets.UTF_8), JoinChannelResponse.class);
        assertThat(channelsResponse.getChannel()).isEqualTo("1234567");
        assertThat(channelsResponse.getIsHandraiseEnabled()).isEqualTo(true);
        assertThat(channelsResponse.getUsers().get(0)).satisfies(channelResponse -> {
            assertThat(channelResponse.getName()).isEqualTo("Mac Hereford");
            assertThat(channelResponse.getIsSpeaker()).isEqualTo(true);
        });

    }

    @Test
    public void testGetChannelOk() throws IOException {
        String token = testLoginService.login(false, mockServer, testClientBuilder);

        // join_channel
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(testLoginService.getRequiredLoginHeaders())
                        .withBody(json("{" + " \"channel\": \"1234567\"}", MatchType.ONLY_MATCHING_FIELDS))
                        .withPath("/get_channel"))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(classPathUtils.getResourceAsString("responses/get_channel.json",
                                StandardCharsets.UTF_8)));

        byte[] responseBody = testClientBuilder.build()
                .post()
                .uri("/api/get_channel")
                .bodyValue("{" + " \"channel\": \"1234567\"}")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectHeader()
                .valueEquals("Content-Type", "application/json")
                .expectBody()
                .returnResult()
                .getResponseBody();

        JoinChannelResponse channelsResponse = new ObjectMapper()
                .readValue(new String(responseBody, StandardCharsets.UTF_8), JoinChannelResponse.class);
        assertThat(channelsResponse.getChannel()).isEqualTo("1234567");
        assertThat(channelsResponse.getIsHandraiseEnabled()).isEqualTo(true);
        assertThat(channelsResponse.getUsers().get(0)).satisfies(channelResponse -> {
            assertThat(channelResponse.getName()).isEqualTo("Mac Hereford");
            assertThat(channelResponse.getIsSpeaker()).isEqualTo(true);
        });

    }

    @Test
    public void testAcceptSpeakerInviteOk() throws IOException {
        String token = testLoginService.login(false, mockServer, testClientBuilder);

        // accept_speaker_invite
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(testLoginService.getRequiredLoginHeaders())
                        .withPath("/accept_speaker_invite")
                        .withBody(json("{" + " \"channel\": \"1234567\", \"user_id\": 11}",
                                MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2 }")
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        testClientBuilder.build()
                .post()
                .uri("/api/accept_speaker_invite")
                .bodyValue("{" + " \"channel\": \"1234567\", \"user_id\": 11}")
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

    }

    @Test
    public void testRejectSpeakerInviteOk() throws IOException {
        String token = testLoginService.login(false, mockServer, testClientBuilder);

        // accept_speaker_invite
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(testLoginService.getRequiredLoginHeaders())
                        .withPath("/reject_speaker_invite")
                        .withBody(json("{" + " \"channel\": \"1234567\", \"user_id\": 11}",
                                MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2 }")
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        testClientBuilder.build()
                .post()
                .uri("/api/reject_speaker_invite")
                .bodyValue("{" + " \"channel\": \"1234567\", \"user_id\": 11}")
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

    }

    @Test
    public void leaveChannelOk() throws IOException {
        String token = testLoginService.login(false, mockServer, testClientBuilder);

        // active_ping
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(testLoginService.getRequiredLoginHeaders())
                        .withPath("/leave_channel")
                        .withBody(json("{" + " \"channel\": \"1234567\"}", MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2}")
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        testClientBuilder.build()
                .post()
                .uri("/api/leave_channel")
                .bodyValue("{" + " \"channel\": \"1234567\"}")
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

    }

    @Test
    public void raiseHandsOk() throws IOException {
        String token = testLoginService.login(false, mockServer, testClientBuilder);

        // active_ping
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(testLoginService.getRequiredLoginHeaders())
                        .withPath("/audience_reply")
                        .withBody(json(
                                "{" + " \"channel\": \"1234567\", \"raise_hands\": true, \"unraise_hands\": false}",
                                MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2}")
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        // audience_reply 2
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(testLoginService.getRequiredLoginHeaders())
                        .withPath("/audience_reply")
                        .withBody(json(
                                "{" + " \"channel\": \"1234567\", \"raise_hands\": false, \"unraise_hands\": true}",
                                MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2}")
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        testClientBuilder.build()
                .post()
                .uri("/api/audience_reply")
                .bodyValue("{" + " \"channel\": \"1234567\", \"raise_hands\": false, \"unraise_hands\": true}")
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

        testClientBuilder.build()
                .post()
                .uri("/api/audience_reply")
                .bodyValue("{" + " \"channel\": \"1234567\", \"raise_hands\": true, \"unraise_hands\": false}")
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

    }

    @Test
    public void testGetEventsOk() throws IOException {
        String token = testLoginService.login(false, mockServer, testClientBuilder);

        // get_suggested_follows_all
        mockServer
                .when(request().withMethod(HttpMethod.GET.name())
                        .withHeaders(testLoginService.getRequiredLoginHeaders())
                        .withPath("/get_events")
                        .withQueryStringParameter("is_filtered", "true")
                        .withQueryStringParameter("page_size", String.valueOf(50))
                        .withQueryStringParameter("page", String.valueOf(1)))
                .respond(
                        response().withStatusCode(HttpStatus.OK.value())
                                .withContentType(MediaType.APPLICATION_JSON)
                                .withBody(classPathUtils.getResourceAsString("responses/get_events.json",
                                        StandardCharsets.UTF_8), StandardCharsets.UTF_8)
                                .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        byte[] responseBody = testClientBuilder.build()
                .get()
                .uri("/api/get_events?page=1&page_size=50")
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

        GetEventsResponse suggestedFollowsResponse =
                new ObjectMapper().readValue(new String(responseBody, StandardCharsets.UTF_8), GetEventsResponse.class);
        assertThat(suggestedFollowsResponse.getCount()).isEqualTo(80);
        assertThat(suggestedFollowsResponse.getEvents().get(0)).satisfies(getEventEventResponse -> {
            assertThat(getEventEventResponse.getName()).isEqualTo("Neuro: Бикамеральный разум и “Мир Дикого запада”");
            assertThat(getEventEventResponse.getClub()).isEqualTo(null);
            assertThat(getEventEventResponse.getHosts().get(0).getName()).isEqualTo("Andrey Stepanov");
            assertThat(getEventEventResponse.getChannel()).isEqualTo(null);
        });

        assertThat(suggestedFollowsResponse.getEvents().get(1)).satisfies(getEventEventResponse -> {
            assertThat(getEventEventResponse.getClub().getName()).isEqualTo("Small Steps & Giant Leaps");
            assertThat(getEventEventResponse.getChannel()).isEqualTo("MOABDYq0");
            assertThat(getEventEventResponse.getTimeStart()).isEqualTo("2021-03-04T08:00:00+00:00");
        });

    }

    @Test
    public void testInviteSpeakerOk() throws IOException {
        String token = testLoginService.login(false, mockServer, testClientBuilder);

        // accept_speaker_invite
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(testLoginService.getRequiredLoginHeaders())
                        .withPath("/invite_speaker")
                        .withBody(json("{" + " \"channel\": \"1234567\", \"user_id\": 11}",
                                MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2 }")
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        testClientBuilder.build()
                .post()
                .uri("/api/invite_speaker")
                .bodyValue("{" + " \"channel\": \"1234567\", \"user_id\": 11}")
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

    }

    @Test
    public void testUnInviteSpeakerOk() throws IOException {
        String token = testLoginService.login(false, mockServer, testClientBuilder);

        // accept_speaker_invite
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(testLoginService.getRequiredLoginHeaders())
                        .withPath("/uninvite_speaker")
                        .withBody(json("{" + " \"channel\": \"1234567\", \"user_id\": 11}",
                                MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2 }")
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        testClientBuilder.build()
                .post()
                .uri("/api/uninvite_speaker")
                .bodyValue("{" + " \"channel\": \"1234567\", \"user_id\": 11}")
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

    }

    @Test
    public void testMakeModerator() throws IOException {
        String token = testLoginService.login(false, mockServer, testClientBuilder);

        // accept_speaker_invite
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(testLoginService.getRequiredLoginHeaders())
                        .withPath("/make_moderator")
                        .withBody(json("{" + " \"channel\": \"1234567\", \"user_id\": 11}",
                                MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2 }")
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        testClientBuilder.build()
                .post()
                .uri("/api/make_moderator")
                .bodyValue("{" + " \"channel\": \"1234567\", \"user_id\": 11}")
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

    }

    @Test
    public void testBlock() throws IOException {
        String token = testLoginService.login(false, mockServer, testClientBuilder);

        // accept_speaker_invite
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(testLoginService.getRequiredLoginHeaders())
                        .withPath("/block_from_channel")
                        .withBody(json("{" + " \"channel\": \"1234567\", \"user_id\": 11}",
                                MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2 }")
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        testClientBuilder.build()
                .post()
                .uri("/api/block_from_channel")
                .bodyValue("{" + " \"channel\": \"1234567\", \"user_id\": 11}")
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

    }
}
