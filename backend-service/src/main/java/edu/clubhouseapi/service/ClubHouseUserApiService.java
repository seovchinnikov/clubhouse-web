package edu.clubhouseapi.service;

import edu.clubhouseapi.config.ClubHouseConfigProperties;
import edu.clubhouseapi.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;

@Service
public class ClubHouseUserApiService {

    @Autowired
    @Qualifier("clubHouseWebClientBuilder")
    WebClient.Builder clubHouseWebClientBuilder;

    @Autowired
    ClubHouseConfigProperties clubHouseConfigProperties;

    @Autowired
    ClubHouseStaticFilesService clubHouseStaticFilesService;

    public Mono<ProfileResponse> getUserProfile(ProfileRequest profileRequest) {
        String url = clubHouseConfigProperties.getApiUrl() + "/get_profile";
        return clubHouseWebClientBuilder.build()
                .post()
                .uri(url)
                .bodyValue(profileRequest)
                .retrieve()
                .bodyToMono(ProfileResponse.class)
                .map(channelsResponse -> {
                    if (channelsResponse.getUserProfile() != null) {

                        channelsResponse.getUserProfile()
                                .setPhotoUrl(clubHouseStaticFilesService
                                        .encodeUrl(channelsResponse.getUserProfile().getPhotoUrl()));
                    }

                    if (channelsResponse.getUserProfile() != null
                            && channelsResponse.getUserProfile().getClubs() != null) {
                        channelsResponse.getUserProfile()
                                .getClubs()
                                .forEach(club -> club
                                        .setPhotoUrl(clubHouseStaticFilesService.encodeUrl(club.getPhotoUrl())));
                    }

                    return channelsResponse;
                })
                .switchIfEmpty(Mono.defer(
                        () -> Mono.just(ProfileResponse.builder().success(false).errorMessage("Empty").build())));
    }

    public Mono<SuggestedFollowsResponse> getSuggestedFollowsAll(Integer pageSize, Integer page) {
        pageSize = pageSize != null ? pageSize : 50;
        page = page != null ? page : 1;
        String url = clubHouseConfigProperties.getApiUrl()
                + String.format("/get_suggested_follows_all?in_onboarding=false&page_size=%s&page=%s", pageSize, page);
        return clubHouseWebClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(SuggestedFollowsResponse.class)
                .map(suggestedFollowsResponse -> {
                    if (suggestedFollowsResponse.getUsers() == null) {
                        return suggestedFollowsResponse;
                    }
                    for (final SuggestedFollowsResponse.SuggestedFollowsResponseUser user : suggestedFollowsResponse
                            .getUsers()) {
                        user.setPhotoUrl(clubHouseStaticFilesService.encodeUrl(user.getPhotoUrl()));
                    }

                    return suggestedFollowsResponse;
                })
                .switchIfEmpty(Mono.defer(() -> Mono
                        .just(SuggestedFollowsResponse.builder().success(false).errorMessage("Empty").build())));
    }

    public Mono<MeResponse> me(MeRequest profileRequest) {
        String url = clubHouseConfigProperties.getApiUrl() + "/me";
        MeRequest request = profileRequest.toBuilder().build();
        if (request.getReturnBlockedIds() == null) {
            request.setReturnBlockedIds(true);
        }
        if (request.getReturnFollowingIds() == null) {
            request.setReturnFollowingIds(true);
        }
        return clubHouseWebClientBuilder.build()
                .post()
                .uri(url)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(MeResponse.class)
                .map(meResponse -> {
                    if (meResponse.getUserProfile() != null) {
                        meResponse.getUserProfile()
                                .setPhotoUrl(clubHouseStaticFilesService
                                        .encodeUrl(meResponse.getUserProfile().getPhotoUrl()));
                    }
                    return meResponse;
                })
                .switchIfEmpty(
                        Mono.defer(() -> Mono.just(MeResponse.builder().success(false).errorMessage("Empty").build())));
    }

    public Mono<EmptyResponse> follow(FollowUnfollowRequest followUnfollowRequest) {
        String url = clubHouseConfigProperties.getApiUrl() + "/follow";

        return clubHouseWebClientBuilder.build().post().uri(url).bodyValue(new HashMap<String, Object>() {

            {
                put("source", 4);
                put("user_id", followUnfollowRequest.getUserId());
            }
        })
                .retrieve()
                .bodyToMono(EmptyResponse.class)
                .switchIfEmpty(Mono
                        .defer(() -> Mono.just(EmptyResponse.builder().success(false).errorMessage("Empty").build())));
    }

    public Mono<EmptyResponse> unfollow(FollowUnfollowRequest followUnfollowRequest) {
        String url = clubHouseConfigProperties.getApiUrl() + "/unfollow";
        return clubHouseWebClientBuilder.build()
                .post()
                .uri(url)
                .bodyValue(followUnfollowRequest)
                .retrieve()
                .bodyToMono(EmptyResponse.class)
                .switchIfEmpty(Mono
                        .defer(() -> Mono.just(EmptyResponse.builder().success(false).errorMessage("Empty").build())));
    }

    public Mono<UserSearchResponse> userSearch(SearchRequest searchRequest) {
        String url = clubHouseConfigProperties.getApiUrl()
                + "/search_users"
                + "?page_size="
                + searchRequest.getPageSize()
                + "&page="
                + searchRequest.getPage();
        return clubHouseWebClientBuilder.build()
                .post()
                .uri(url)
                .bodyValue(searchRequest)
                .retrieve()
                .bodyToMono(UserSearchResponse.class)
                .map(userSearchResponse -> {
                    userSearchResponse.getUsers()
                            .forEach(userSearchResponseUser -> userSearchResponseUser.setPhotoUrl(
                                    clubHouseStaticFilesService.encodeUrl(userSearchResponseUser.getPhotoUrl())));
                    return userSearchResponse;
                })
                .switchIfEmpty(Mono.defer(
                        () -> Mono.just(UserSearchResponse.builder().success(false).errorMessage("Empty").build())));
    }

    public Mono<FollowersResponse> getFollowers(String userId, Integer pageSize, Integer page) {
        String url = clubHouseConfigProperties.getApiUrl()
                + "/get_followers"
                + "?user_id="
                + userId
                + "&page_size="
                + pageSize
                + "&page="
                + page;
        return clubHouseWebClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(FollowersResponse.class)
                .map(userSearchResponse -> {
                    userSearchResponse.getUsers()
                            .forEach(userSearchResponseUser -> userSearchResponseUser.setPhotoUrl(
                                    clubHouseStaticFilesService.encodeUrl(userSearchResponseUser.getPhotoUrl())));
                    return userSearchResponse;
                })
                .switchIfEmpty(Mono.defer(
                        () -> Mono.just(FollowersResponse.builder().success(false).errorMessage("Empty").build())));
    }

    public Mono<FollowingResponse> getFollowing(String userId, Integer pageSize, Integer page) {
        String url = clubHouseConfigProperties.getApiUrl()
                + "/get_following"
                + "?user_id="
                + userId
                + "&page_size="
                + pageSize
                + "&page="
                + page;
        return clubHouseWebClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(FollowingResponse.class)
                .map(userSearchResponse -> {
                    userSearchResponse.getUsers()
                            .forEach(userSearchResponseUser -> userSearchResponseUser.setPhotoUrl(
                                    clubHouseStaticFilesService.encodeUrl(userSearchResponseUser.getPhotoUrl())));
                    return userSearchResponse;
                })
                .switchIfEmpty(Mono.defer(
                        () -> Mono.just(FollowingResponse.builder().success(false).errorMessage("Empty").build())));
    }

    public Mono<NotificationsResponse> getNotifications(Integer pageSize, Integer page) {
        String url = clubHouseConfigProperties.getApiUrl()
                + "/get_notifications"
                + "?page_size="
                + pageSize
                + "&page="
                + page;
        return clubHouseWebClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(NotificationsResponse.class)
                .map(notificationsResponse -> {
                    notificationsResponse.getNotifications().forEach(notification -> {
                        if (notification.getUserProfile() == null) {
                            return;
                        }
                        notification.getUserProfile()
                                .setPhotoUrl(clubHouseStaticFilesService
                                        .encodeUrl(notification.getUserProfile().getPhotoUrl()));
                    });
                    return notificationsResponse;
                })
                .switchIfEmpty(Mono.defer(
                        () -> Mono.just(NotificationsResponse.builder().success(false).errorMessage("Empty").build())));
    }

    public Mono<EmptyResponse> updateBio(UpdateBioRequest updateBioRequest) {
        return clubHouseWebClientBuilder.baseUrl(clubHouseConfigProperties.getApiUrl()).build()
                .post()
                .uri("/update_bio")
                .bodyValue(updateBioRequest)
                .retrieve()
                .bodyToMono(EmptyResponse.class)
                .switchIfEmpty(Mono.just(EmptyResponse.failure()));
    }

    public Mono<EmptyResponse> updateName(UpdateNameRequest updateNameRequest) {
        return clubHouseWebClientBuilder.baseUrl(clubHouseConfigProperties.getApiUrl()).build()
                .post()
                .uri("/update_name")
                .bodyValue(updateNameRequest)
                .retrieve()
                .bodyToMono(EmptyResponse.class)
                .switchIfEmpty(Mono.just(EmptyResponse.failure()));
    }

    public Mono<EmptyResponse> updateUsername(UpdateUsernameRequest updateUsernameRequest) {
        return clubHouseWebClientBuilder.baseUrl(clubHouseConfigProperties.getApiUrl()).build()
                .post()
                .uri("/update_username")
                .bodyValue(updateUsernameRequest)
                .retrieve()
                .bodyToMono(EmptyResponse.class)
                .switchIfEmpty(Mono.just(EmptyResponse.failure()));
    }
}
