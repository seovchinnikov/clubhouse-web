package edu.clubhouseapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChannelsResponse {

    @JsonProperty("channels")
    List<ChannelResponse> channels;

    @JsonProperty("success")
    Boolean success;

    @JsonProperty("error_message")
    private String errorMessage;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder(toBuilder = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChannelResponse {

        @JsonProperty("channel")
        String channel;

        @JsonProperty("topic")
        String topic;

        @JsonProperty("is_private")
        boolean isPrivate;

        @JsonProperty("url")
        String url;

        @JsonProperty("creator_user_profile_id")
        Integer creatorUserProfileId;

        @JsonProperty("num_speakers")
        Integer numSpeakers;

        @JsonProperty("num_all")
        Integer numAll;

        @JsonProperty("users")
        List<ChannelUsersResponse> users;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder(toBuilder = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChannelUsersResponse {

        @JsonProperty("user_id")
        Integer userId;

        @JsonProperty("is_speaker")
        boolean isSpeaker;

        @JsonProperty("is_moderator")
        boolean isModerator;

        @JsonProperty("name")
        String name;

        @JsonProperty("photo_url")
        String photoUrl;
    }
}
