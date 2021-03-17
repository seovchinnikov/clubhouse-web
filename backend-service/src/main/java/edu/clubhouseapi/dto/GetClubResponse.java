package edu.clubhouseapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetClubResponse {

    @NotNull
    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("is_admin")
    private Boolean isAdmin;

    @JsonProperty("is_member")
    private Boolean isMember;

    @JsonProperty("is_follower")
    private Boolean isFollower;

    @JsonProperty("club")
    private ClubInfo clubInfo;

    @JsonProperty("error_message")
    private String errorMessage;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ClubInfo {
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

        @JsonProperty("is_follow_allowed")
        private Boolean isFollowAllowed;

        @JsonProperty("is_membership_private")
        private Boolean isMembershipPrivate;

        @JsonProperty("is_community")
        private Boolean isCommunity;
    }
}
