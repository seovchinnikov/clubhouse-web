package edu.clubhouseapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder=true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MeRequest {

    @NotNull
    @JsonProperty("timezone_identifier")
    private String timezoneIdentifier;


    @JsonProperty("return_blocked_ids")
    private Boolean returnBlockedIds = true;

    @JsonProperty("return_following_ids")
    private Boolean returnFollowingIds = true;
}
