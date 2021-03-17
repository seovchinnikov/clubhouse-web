package edu.clubhouseapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfileResponse {

    @NotNull
    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("user_profile")
    private ProfileResponseInner userProfile;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProfileResponseInner {

        @JsonProperty("name")
        private String name;

        @JsonProperty("username")
        private String username;

        @JsonProperty("bio")
        private String bio;

        @JsonProperty("twitter")
        private String twitter;

        @JsonProperty("instagram")
        private String instagram;

        @JsonProperty("num_followers")
        private Integer numFollowers;

        @JsonProperty("num_following")
        private Integer numFollowing;

        @JsonProperty("follows_me")
        private Boolean followsMe;

        @JsonProperty("mutual_follows_count")
        private Integer mutualFollowsCount;

        @JsonProperty("photo_url")
        private String photoUrl;

        @JsonProperty("clubs")
        private List<ProfileClubResponse> clubs;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProfileClubResponse {

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
