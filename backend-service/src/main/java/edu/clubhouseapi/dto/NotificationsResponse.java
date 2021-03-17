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
public class NotificationsResponse {

    @JsonProperty("count")
    private Integer count;

    @JsonProperty("next")
    private Integer next;

    @JsonProperty("previous")
    private Integer previous;

    @JsonProperty("notifications")
    private List<Notification> notifications;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Notification {

        @JsonProperty("message")
        private String message;

        @JsonProperty("type")
        private Integer type;

        @JsonProperty("is_unread")
        private Boolean isUnread;

        @JsonProperty("time_created")
        private String timeCreated;

        @JsonProperty("user_profile")
        NotificationUserProfile userProfile;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder(toBuilder = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NotificationUserProfile {

        @JsonProperty("user_id")
        String userId;

        @JsonProperty("username")
        String username;

        @JsonProperty("name")
        String name;

        @JsonProperty("photo_url")
        String photoUrl;
    }

    @NotNull
    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("error_message")
    private String errorMessage;
}
