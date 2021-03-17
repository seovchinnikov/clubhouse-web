package edu.clubhouseapi.service;

import edu.clubhouseapi.config.ClubHouseConfigProperties;
import edu.clubhouseapi.dto.GetClubRequest;
import edu.clubhouseapi.dto.GetClubResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ClubHouseClubsApiService {

    @Autowired
    @Qualifier("clubHouseWebClientBuilder")
    WebClient.Builder clubHouseWebClientBuilder;

    @Autowired
    ClubHouseConfigProperties clubHouseConfigProperties;

    @Autowired
    ClubHouseStaticFilesService clubHouseStaticFilesService;

    public Mono<GetClubResponse> getClub(GetClubRequest getClubRequest) {
        String url = clubHouseConfigProperties.getApiUrl() + "/get_club";
        return clubHouseWebClientBuilder.build()
                .post()
                .uri(url)
                .bodyValue(getClubRequest)
                .retrieve()
                .bodyToMono(GetClubResponse.class)
                .switchIfEmpty(Mono.defer(
                        () -> Mono.just(GetClubResponse.builder().success(false).errorMessage("Empty").build())));

    }
}
