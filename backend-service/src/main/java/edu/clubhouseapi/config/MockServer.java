package edu.clubhouseapi.config;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

import edu.clubhouseapi.util.ByteUtils;
import edu.clubhouseapi.util.ClassPathUtils;
import org.apache.commons.io.IOUtils;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.MatchType;
import org.mockserver.model.Header;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@Profile("dev")
public class MockServer implements InitializingBean, DisposableBean {

    protected ClientAndServer mockServer;

    @Autowired
    ByteUtils byteUtils;

    @Autowired
    AppConfigProperties appConfigProperties;

    @Autowired
    ClassPathUtils classPathUtils;

    public static final String USER_ID = "999";

    public static final String TOKEN_PREFIX = "Token ";

    public static final String TOKEN = "7b11922920432da76728b216e202cecf7ade31a2";

    public static final String NEW_TOKEN = "new_token";

    public static final String NEW_REFRESH_TOKEN = "new_refresh_token";

    public static final String REFRESH_TOKEN = "refresh_token";

    public static final String PHONE_NUMBER = "+89606666666";

    public static final String VERIFICATION_CODE = "1234";

    @Override
    public void afterPropertiesSet() throws IOException {
        mockServer = ClientAndServer.startClientAndServer(appConfigProperties.getMockPort());
        configure();
    }

    protected void configure() throws IOException {
        String cookie = "__cfduid=" + appConfigProperties.getMockCookie();

        List<Header> headers = getBasicHeaders();
        headers.add(new Header("CH-UserID", "(null)"));
        headers.add(new Header("CH-DeviceId", byteUtils.fixedUuidFromString(cookie)));
        headers.add(new Header("Cookie", cookie));

        // /start_phone_number_auth
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(headers)
                        .withPath("/start_phone_number_auth")
                        .withBody(json("{" + " \"phone_number\": \"" + PHONE_NUMBER + "\"}",
                                MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2}"));
        mockServer.when(
                request().withMethod(HttpMethod.POST.name()).withHeaders(headers).withPath("/start_phone_number_auth"))
                .respond(response().withStatusCode(HttpStatus.UNAUTHORIZED.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": false"));

        // /complete_phone_number_auth
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
                        .withBody(classPathUtils.getResourceAsString("responses/complete_phone_response.json",
                                StandardCharsets.UTF_8), StandardCharsets.UTF_8)
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        // /check_waitlist_status
        headers.remove(new Header("CH-UserID", "(null)"));
        headers.add(new Header("CH-UserID", USER_ID));
        headers.add(new Header("Authorization", TOKEN_PREFIX + TOKEN));
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(headers)
                        .withPath("/check_waitlist_status")
                        .withBody(""))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2, \"is_waitlisted\": false }"));

        // /refresh_token
        mockServer.when(request().withMethod(HttpMethod.POST.name())
                .withHeaders(headers)
                .withPath("/refresh_token")
                .withBody(json("{" + " \"refresh\": \"" + REFRESH_TOKEN + "\"}", MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2, \"access\": \""
                                + NEW_TOKEN
                                + "\", "
                                + " \"refresh\": \""
                                + NEW_REFRESH_TOKEN
                                + "\" }"));

        // /refresh_token for NEW_TOKEN
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(replaceHeader(headers,
                                new Header("Authorization", TOKEN_PREFIX + TOKEN),
                                new Header("Authorization", TOKEN_PREFIX + NEW_TOKEN)))
                        .withPath("/refresh_token")
                        .withBody(json("{" + " \"refresh\": \"" + NEW_REFRESH_TOKEN + "\"}",
                                MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2, \"access\": \""
                                + NEW_TOKEN
                                + "\", "
                                + " \"refresh\": \""
                                + NEW_REFRESH_TOKEN
                                + "\" }"));

        // get_channels
        mockServer.when(request().withMethod(HttpMethod.GET.name()).withHeaders(headers).withPath("/get_channels"))
                .respond(
                        response().withStatusCode(HttpStatus.OK.value())
                                .withContentType(MediaType.APPLICATION_JSON)
                                .withBody(classPathUtils.getResourceAsString("responses/get_channels.json",
                                        StandardCharsets.UTF_8), StandardCharsets.UTF_8)
                                .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        // get_profile
        mockServer.when(request().withMethod(HttpMethod.POST.name()).withHeaders(headers).withPath("/get_profile"))
                .respond(
                        response().withStatusCode(HttpStatus.OK.value())
                                .withContentType(MediaType.APPLICATION_JSON)
                                .withBody(classPathUtils.getResourceAsString("responses/get_profile.json",
                                        StandardCharsets.UTF_8), StandardCharsets.UTF_8)
                                .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        // get_suggested_follows_all
        mockServer
                .when(request().withMethod(HttpMethod.GET.name())
                        .withHeaders(headers)
                        .withPath("/get_suggested_follows_all")
                        .withQueryStringParameter("in_onboarding", "false")
                        .withQueryStringParameter("page_size", String.valueOf(50))
                        .withQueryStringParameter("page", String.valueOf(1)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(classPathUtils.getResourceAsString("responses/get_suggested_follows_all.json",
                                StandardCharsets.UTF_8), StandardCharsets.UTF_8)
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));
        mockServer
                .when(request().withMethod(HttpMethod.GET.name())
                        .withHeaders(headers)
                        .withPath("/get_suggested_follows_all")
                        .withQueryStringParameter("in_onboarding", "false")
                        .withQueryStringParameter("page_size", String.valueOf(50))
                        .withQueryStringParameter("page", String.valueOf(2)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(classPathUtils.getResourceAsString("responses/get_suggested_follows_all2.json",
                                StandardCharsets.UTF_8), StandardCharsets.UTF_8)
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));
        // active_ping
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(headers)
                        .withPath("/active_ping")
                        .withBody(json("{" + " \"channel\": \"1234567\"}", MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2, \"should_leave\": false }")
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        // join_channel
        mockServer.when(request().withMethod(HttpMethod.POST.name())
                .withHeaders(headers)
                .withPath("/join_channel")
                .withBody(json("{"
                        + " \"channel\": \"1234567\", \"attribution_source\": \"feed\", \"attribution_details\": \"eyJpc19leHBsb3JlIjpmYWxzZSwicmFuayI6MX0=\"}",
                        MatchType.ONLY_MATCHING_FIELDS)))
                .respond(
                        response().withStatusCode(HttpStatus.OK.value())
                                .withContentType(MediaType.APPLICATION_JSON)
                                .withBody(classPathUtils.getResourceAsString("responses/join_channel.json",
                                        StandardCharsets.UTF_8), StandardCharsets.UTF_8)
                                .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        // get_channel
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(headers)
                        .withPath("/get_channel")
                        .withBody(json("{" + " \"channel\": \"1234567\"}", MatchType.ONLY_MATCHING_FIELDS)))
                .respond(
                        response().withStatusCode(HttpStatus.OK.value())
                                .withContentType(MediaType.APPLICATION_JSON)
                                .withBody(classPathUtils.getResourceAsString("responses/get_channel.json",
                                        StandardCharsets.UTF_8), StandardCharsets.UTF_8)
                                .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        // accept_speaker_invite
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(headers)
                        .withPath("/accept_speaker_invite")
                        .withBody(json("{" + " \"channel\": \"1234567\", \"user_id\": 11}",
                                MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2}")
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        // reject_speaker_invite
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(headers)
                        .withPath("/reject_speaker_invite")
                        .withBody(json("{" + " \"channel\": \"1234567\", \"user_id\": 11}",
                                MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2}")
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        // leave_channel
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(headers)
                        .withPath("/leave_channel")
                        .withBody(json("{" + " \"channel\": \"1234567\"}", MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2}")
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));
        // audience_reply
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(headers)
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
                        .withHeaders(headers)
                        .withPath("/audience_reply")
                        .withBody(json(
                                "{" + " \"channel\": \"1234567\", \"raise_hands\": false, \"unraise_hands\": true}",
                                MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2}")
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        // get_events
        mockServer
                .when(request().withMethod(HttpMethod.GET.name())
                        .withHeaders(headers)
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

        mockServer
                .when(request().withMethod(HttpMethod.GET.name())
                        .withHeaders(headers)
                        .withPath("/get_events")
                        .withQueryStringParameter("is_filtered", "true")
                        .withQueryStringParameter("page_size", String.valueOf(50))
                        .withQueryStringParameter("page", String.valueOf(2)))
                .respond(
                        response().withStatusCode(HttpStatus.OK.value())
                                .withContentType(MediaType.APPLICATION_JSON)
                                .withBody(classPathUtils.getResourceAsString("responses/get_events2.json",
                                        StandardCharsets.UTF_8), StandardCharsets.UTF_8)
                                .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));
        // me
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(headers)
                        .withPath("/me")
                        .withBody(json("{" + " \"return_blocked_ids\": true, \"return_following_ids\": true}",
                                MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(classPathUtils.getResourceAsString("responses/me.json", StandardCharsets.UTF_8),
                                StandardCharsets.UTF_8)
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        // follow
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(headers)
                        .withPath("/follow")
                        .withBody(json("{" + " \"source\": 4}", MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2}")
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        // unfollow
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(headers)
                        .withPath("/unfollow")
                        .withBody(json("{}", MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2}")
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        // get_club
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(headers)
                        .withPath("/get_club")
                        .withBody(json("{}", MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(classPathUtils.getResourceAsString("responses/get_club.json", StandardCharsets.UTF_8),
                                StandardCharsets.UTF_8)
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        // search_users
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(headers)
                        .withPath("/search_users")
                        .withBody(json(
                                "{"
                                        + " \"cofollows_only\": false,"
                                        + " \"following_only\": false, \"followers_only\": false,"
                                        + " \"query\": \"elon\", \"page_size\": 50, \"page\": 1}",
                                MatchType.ONLY_MATCHING_FIELDS)))
                .respond(
                        response().withStatusCode(HttpStatus.OK.value())
                                .withContentType(MediaType.APPLICATION_JSON)
                                .withBody(classPathUtils.getResourceAsString("responses/search_users.json",
                                        StandardCharsets.UTF_8), StandardCharsets.UTF_8)
                                .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(headers)
                        .withPath("/search_users")
                        .withBody(json(
                                "{"
                                        + " \"cofollows_only\": false,"
                                        + " \"following_only\": false, \"followers_only\": false,"
                                        + " \"query\": \"elon\", \"page_size\": 50, \"page\": 2}",
                                MatchType.ONLY_MATCHING_FIELDS)))
                .respond(
                        response().withStatusCode(HttpStatus.OK.value())
                                .withContentType(MediaType.APPLICATION_JSON)
                                .withBody(classPathUtils.getResourceAsString("responses/search_users.json",
                                        StandardCharsets.UTF_8), StandardCharsets.UTF_8)
                                .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));
        //get_following
        mockServer
                .when(request().withMethod(HttpMethod.GET.name())
                        .withHeaders(headers)
                        .withPath("/get_following")
                        .withQueryStringParameter("page_size", String.valueOf(50))
                        .withQueryStringParameter("page", String.valueOf(1)))
                .respond(
                        response().withStatusCode(HttpStatus.OK.value())
                                .withContentType(MediaType.APPLICATION_JSON)
                                .withBody(classPathUtils.getResourceAsString("responses/get_following.json",
                                        StandardCharsets.UTF_8), StandardCharsets.UTF_8)
                                .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        //get_followers
        mockServer
                .when(request().withMethod(HttpMethod.GET.name())
                        .withHeaders(headers)
                        .withPath("/get_followers")
                        .withQueryStringParameter("page_size", String.valueOf(50))
                        .withQueryStringParameter("page", String.valueOf(1)))
                .respond(
                        response().withStatusCode(HttpStatus.OK.value())
                                .withContentType(MediaType.APPLICATION_JSON)
                                .withBody(classPathUtils.getResourceAsString("responses/get_followers.json",
                                        StandardCharsets.UTF_8), StandardCharsets.UTF_8)
                                .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        //get_notifications
        mockServer
                .when(request().withMethod(HttpMethod.GET.name())
                        .withHeaders(headers)
                        .withPath("/get_notifications")
                        .withQueryStringParameter("page_size", String.valueOf(50))
                        .withQueryStringParameter("page", String.valueOf(1)))
                .respond(
                        response().withStatusCode(HttpStatus.OK.value())
                                .withContentType(MediaType.APPLICATION_JSON)
                                .withBody(classPathUtils.getResourceAsString("responses/get_notifications.json",
                                        StandardCharsets.UTF_8), StandardCharsets.UTF_8)
                                .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        // invite_speaker
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(headers)
                        .withPath("/invite_speaker")
                        .withBody(json("{}",
                                MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2}")
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        // uninvite_speaker
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(headers)
                        .withPath("/uninvite_speaker")
                        .withBody(json("{}",
                                MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2}")
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        // make_moderator
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(headers)
                        .withPath("/make_moderator")
                        .withBody(json("{}",
                                MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2}")
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        // block_from_channel
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(headers)
                        .withPath("/block_from_channel")
                        .withBody(json("{}",
                                MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true, \"unused_attr\": 2}")
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        // update_bio
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(headers)
                        .withPath("/update_bio")
                        .withBody(json("{\"bio\": \"test error\"}", MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.BAD_REQUEST.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": false, \"error\": \"Bad request\"}")
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(headers)
                        .withPath("/update_bio")
                        .withBody(json("{}", MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true}")
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        //  update_name
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(headers)
                        .withPath("/update_name")
                        .withBody(json("{\"name\": \"test error\"}", MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.BAD_REQUEST.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": false, \"error\": \"Bad request\"}")
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(headers)
                        .withPath("/update_name")
                        .withBody(json("{}", MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true}")
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));

        //  update_username
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(headers)
                        .withPath("/update_username")
                        .withBody(json("{\"username\": \"test error\"}", MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.BAD_REQUEST.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": false, \"error\": \"Bad request\"}")
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));
        mockServer
                .when(request().withMethod(HttpMethod.POST.name())
                        .withHeaders(headers)
                        .withPath("/update_username")
                        .withBody(json("{}", MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"success\": true}")
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8")));
    }

    protected List<Header> replaceHeader(List<Header> headers, Header what, Header by) {
        ArrayList<Header> headersNew = new ArrayList<>(headers);
        headersNew.remove(what);
        headersNew.add(by);
        return headersNew;
    }

    @Override
    public void destroy() {
        mockServer.stop();
    }

    public List<Header> getBasicHeaders() {
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("CH-Languages", "en-JP,ja-JP"));
        headers.add(new Header("CH-Locale", "en_JP"));
        headers.add(new Header("Accept", "application/json"));
        headers.add(new Header("Accept-Language", "en-JP;q=1, ja-JP;q=0.9"));
        headers.add(new Header("Accept-Encoding", "gzip, deflate"));
        headers.add(new Header("CH-AppBuild", "304"));
        headers.add(new Header("CH-AppVersion", "0.1.28"));
        headers.add(new Header("User-Agent", "clubhouse/304 (iPhone; iOS 14.4; Scale/2.00)"));
        headers.add(new Header("Connection", "close"));
        headers.add(new Header("Content-Type", "application/json; charset=utf-8"));

        return headers;
    }
}
