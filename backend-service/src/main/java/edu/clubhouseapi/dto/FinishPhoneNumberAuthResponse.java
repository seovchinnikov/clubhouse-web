package edu.clubhouseapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class FinishPhoneNumberAuthResponse {

    @NotNull
    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("auth_token")
    private String authToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("is_waitlisted")
    private Boolean waitListed;

    @JsonProperty("is_onboarding")
    private Boolean onboarding;

    @JsonIgnore
    private String cookie;

    @JsonIgnore
    private String deviceId;

    @JsonIgnore
    String userId;

    @JsonIgnore
    String username;

    @JsonIgnore
    String name;

    @JsonIgnore
    String photoUrl;

    @JsonProperty("user_profile")
    public void setUserProfile(Map<String, Object> userProfile) {
        userId = String.valueOf(userProfile.get("user_id"));
        username = (String) userProfile.get("username");
        name = (String) userProfile.get("name");
        photoUrl = (String) userProfile.get("photo_url");
    }
}
