package edu.clubhouseapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetChannelResponse {

    @JsonProperty("channel_id")
    private String channelId;

    @JsonProperty("channel")
    private String channel;

    @JsonProperty("topic")
    private String topic;

    @JsonProperty("is_private")
    private Boolean isPrivate;

    @JsonProperty("is_social_mode")
    private Boolean isSocialMode;

//    @JsonProperty("club")
//    private String club;

    @JsonProperty("club_name")
    private String clubName;

    @JsonProperty("club_id")
    private String clubId;

    @JsonProperty("is_handraise_enabled")
    private Boolean isHandraiseEnabled;

    @JsonProperty("handraise_permission")
    private Integer handraisePermission;

    @JsonProperty("users")
    List<GetChannelResponseUser> users;

    @NotNull
    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("error_message")
    private String errorMessage;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GetChannelResponseUser {

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

        @JsonProperty("is_speaker")
        private Boolean isSpeaker;

        @JsonProperty("is_muted")
        private Boolean isMuted = true;

        @JsonProperty("is_speaking")
        private Boolean isSpeaking = false;

        @JsonProperty("is_moderator")
        private Boolean isModerator;

        @JsonProperty("is_invited_as_speaker")
        private Boolean isInvitedAsSpeaker;
    }
}
