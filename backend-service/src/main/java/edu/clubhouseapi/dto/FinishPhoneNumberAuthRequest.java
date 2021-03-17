package edu.clubhouseapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@Jacksonized
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class FinishPhoneNumberAuthRequest {

    @NotNull
    @JsonProperty("phone_number")
    private String phoneNumber;

    @NotNull
    @JsonProperty("verification_code")
    private String verificationCode;

    @NotNull
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String cookie;
}
