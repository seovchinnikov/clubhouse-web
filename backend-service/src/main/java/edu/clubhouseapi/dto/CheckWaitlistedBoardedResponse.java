package edu.clubhouseapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckWaitlistedBoardedResponse {

    @JsonProperty("is_waitlisted")
    Boolean isWaitlisted;

    @JsonProperty("is_boarded")
    Boolean isBoarded;

    @JsonProperty("success")
    Boolean success;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("user_body")
    private SecurityUserJwtTokenBody securityUserJwtTokenBody;

    @JsonProperty("token")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String innerToken;
}
