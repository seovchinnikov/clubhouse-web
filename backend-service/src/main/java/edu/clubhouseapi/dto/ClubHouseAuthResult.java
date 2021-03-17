package edu.clubhouseapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Jacksonized
public class ClubHouseAuthResult {

    @JsonProperty("user_id")
    String userId;

    @JsonProperty("username")
    String username;

    @JsonProperty("name")
    String name;

    @JsonProperty("photo_url")
    String photoUrl;

    @JsonProperty("user_token")
    String userToken;

    @JsonProperty("user_device")
    String userDevice;

    @JsonProperty("user_refresh_token")
    String userRefreshToken;

    @JsonProperty("user_cookie")
    String userCookie;

    @JsonProperty("token")
    String innerToken;

    @NotNull
    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("is_waitlisted")
    private Boolean waitListed;

    @JsonProperty("is_onboarding")
    private Boolean onboarding;
}
