package edu.clubhouseapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Jacksonized
public class SecurityUserJwtTokenBody {

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

    @JsonProperty("user_refresh_token")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userRefreshToken;

    @JsonProperty("user_device")
    String userDevice;

    @JsonProperty("user_cookie")
    String userCookie;

    @JsonIgnore
    public boolean isEmpty() {
        return StringUtils.isEmpty(userId) || StringUtils.isEmpty(userToken) || StringUtils.isEmpty(userDevice);
    }

    @JsonProperty("roles")
    private List<String> roles;

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("error_message")
    private String errorMessage;
}
