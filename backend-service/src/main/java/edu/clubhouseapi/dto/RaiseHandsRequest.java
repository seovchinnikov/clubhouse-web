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
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RaiseHandsRequest {

    @NotNull
    @JsonProperty("channel")
    private String channel;

    @NotNull
    @JsonProperty("raise_hands")
    private Boolean raiseHands;

    @NotNull
    @JsonProperty("unraise_hands")
    private Boolean unraiseHands;
}
