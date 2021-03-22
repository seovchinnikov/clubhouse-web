package edu.clubhouseapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Jacksonized
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateNameRequest {
    private String name;
}
