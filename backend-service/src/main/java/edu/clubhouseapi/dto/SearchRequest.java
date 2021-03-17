package edu.clubhouseapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class SearchRequest {
    @NotNull
    @JsonProperty("query")
    private String query;

    @JsonProperty("cofollows_only")
    private Boolean cofollowsOnly = false;

    @JsonProperty("following_only")
    private Boolean followingOnly = false;

    @JsonProperty("followers_only")
    private Boolean followersOnly = false;

    @NotNull
    @JsonProperty("page_size")
    private Integer pageSize = 50;

    @NotNull
    @JsonProperty("page")
    private Integer page = 1;

}
