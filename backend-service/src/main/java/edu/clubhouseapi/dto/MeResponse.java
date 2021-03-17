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
public class MeResponse {

    @JsonProperty("num_invites")
    private Integer numInvites;

    @JsonProperty("has_unread_notifications")
    private Boolean hasUnreadNotifications;

    @JsonProperty("following_ids")
    List<String> followingIds;

    @JsonProperty("blocked_ids")
    List<String> blockedIds;

    @NotNull
    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("user_profile")
    private UserProfile userProfile;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder(toBuilder = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserProfile {

        @JsonProperty("user_id")
        String userId;

        @JsonProperty("username")
        String username;

        @JsonProperty("name")
        String name;

        @JsonProperty("photo_url")
        String photoUrl;
    }
}
