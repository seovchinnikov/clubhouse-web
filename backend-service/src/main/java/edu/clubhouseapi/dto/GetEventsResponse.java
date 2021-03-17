package edu.clubhouseapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Jacksonized
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetEventsResponse {

    @JsonProperty("events")
    private List<GetEventEventResponse> events;

    @NotNull
    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("count")
    private Integer count;

    @JsonProperty("next")
    private Integer next;

    @JsonProperty("previous")
    private Integer previous;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GetEventEventResponse {

        @JsonProperty("event_id")
        private Integer eventId;

        @JsonProperty("name")
        private String name;

        @JsonProperty("description")
        private String description;

        @JsonProperty("time_start")
        private String timeStart;

        @JsonProperty("club")
        private GetEventClubResponse club;

        @JsonProperty("is_member_only")
        private Boolean isMemberOnly;

        @JsonProperty("url")
        private String url;

        @JsonProperty("channel")
        private String channel;

        @JsonProperty("club_is_member")
        private Integer clubIsMember;

        @JsonProperty("club_is_follower")
        private Integer clubIsFollower;

        @JsonProperty("is_expired")
        private Boolean isExpired;

        @JsonProperty("hosts")
        private List<GetEventUserResponse> hosts;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GetEventUserResponse {

        @JsonProperty("user_id")
        private String userId;

        @JsonProperty("name")
        private String name;

        @JsonProperty("photo_url")
        private String photoUrl;

        @JsonProperty("username")
        private String username;

        @JsonProperty("bio")
        private String bio;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GetEventClubResponse {

        @JsonProperty("club_id")
        private Integer clubId;

        @JsonProperty("name")
        private String name;

        @JsonProperty("description")
        private String description;

        @JsonProperty("photo_url")
        private String photoUrl;

        @JsonProperty("num_members")
        private Integer numMembers;

        @JsonProperty("num_followers")
        private Integer numFollowers;

    }
}
