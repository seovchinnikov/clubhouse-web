package edu.clubhouseapi.controller;

import edu.clubhouseapi.dto.EmptyResponse;
import edu.clubhouseapi.dto.FollowUnfollowRequest;
import edu.clubhouseapi.dto.FollowersResponse;
import edu.clubhouseapi.dto.FollowingResponse;
import edu.clubhouseapi.dto.MeRequest;
import edu.clubhouseapi.dto.MeResponse;
import edu.clubhouseapi.dto.NotificationsResponse;
import edu.clubhouseapi.dto.ProfileRequest;
import edu.clubhouseapi.dto.ProfileResponse;
import edu.clubhouseapi.dto.SearchRequest;
import edu.clubhouseapi.dto.SuggestedFollowsResponse;
import edu.clubhouseapi.dto.UserSearchResponse;
import edu.clubhouseapi.service.ClubHouseUserApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@RestController
public class UserController {

    @Autowired
    ClubHouseUserApiService clubHouseUserApiService;

    @PostMapping(value = "/api/get_profile")
    public Mono<ProfileResponse> getChannels(@RequestBody @Valid Mono<ProfileRequest> profileRequestMono) {
        return profileRequestMono.flatMap(x -> clubHouseUserApiService.getUserProfile(x));
    }

    @GetMapping(value = "/api/get_suggested_follows_all")
    public Mono<SuggestedFollowsResponse> getSuggestedFollowsAll(@RequestParam("page_size") @NotNull Integer pageSize,
            @RequestParam("page") @NotNull Integer page) {
        return clubHouseUserApiService.getSuggestedFollowsAll(pageSize, page);
    }

    @PostMapping(value = "/api/me")
    public Mono<MeResponse> me(@RequestBody @Valid Mono<MeRequest> profileRequestMono) {
        return profileRequestMono.flatMap(x -> clubHouseUserApiService.me(x));
    }

    @PostMapping(value = "/api/follow")
    public Mono<EmptyResponse> follow(@RequestBody @Valid Mono<FollowUnfollowRequest> followUnfollowRequestMono) {
        return followUnfollowRequestMono.flatMap(x -> clubHouseUserApiService.follow(x));
    }

    @PostMapping(value = "/api/unfollow")
    public Mono<EmptyResponse> unfollow(@RequestBody @Valid Mono<FollowUnfollowRequest> followUnfollowRequestMono) {
        return followUnfollowRequestMono.flatMap(x -> clubHouseUserApiService.unfollow(x));
    }

    @PostMapping(value = "/api/search_users")
    public Mono<UserSearchResponse> userSearch(@RequestBody @Valid Mono<SearchRequest> searchRequestMono) {
        return searchRequestMono.flatMap(x -> clubHouseUserApiService.userSearch(x));
    }

    @GetMapping(value = "/api/get_followers")
    public Mono<FollowersResponse> getFollowers(@RequestParam("user_id") @NotNull String userId,
            @RequestParam("page_size") @NotNull Integer pageSize,
            @RequestParam("page") @NotNull Integer page) {
        return clubHouseUserApiService.getFollowers(userId, pageSize, page);
    }

    @GetMapping(value = "/api/get_following")
    public Mono<FollowingResponse> getFollowing(@RequestParam("user_id") @NotNull String userId,
            @RequestParam("page_size") @NotNull Integer pageSize,
            @RequestParam("page") @NotNull Integer page) {
        return clubHouseUserApiService.getFollowing(userId, pageSize, page);
    }

    @GetMapping(value = "/api/get_notifications")
    public Mono<NotificationsResponse> getNotifications(@RequestParam("page_size") @NotNull Integer pageSize,
            @RequestParam("page") @NotNull Integer page) {
        return clubHouseUserApiService.getNotifications(pageSize, page);
    }
}
