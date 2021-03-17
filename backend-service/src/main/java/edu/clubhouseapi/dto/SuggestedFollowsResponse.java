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
public class SuggestedFollowsResponse {

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

    @JsonProperty("users")
    private List<SuggestedFollowsResponseUser> users;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SuggestedFollowsResponseUser {

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

}
