package edu.clubhouseapi.controller;

import edu.clubhouseapi.config.ClubHouseConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
public class TestAuthorizedController {

    @Autowired
    ClubHouseConfigProperties clubHouseConfigProperties;

    @Autowired
    WebClient.Builder webClientBuilder;

    @PostMapping(value = "/api/test_auth")
    public Mono<String> testAuth(@RequestBody @Valid Mono<String> mono) {
        return mono.map(x -> x);
    }

    @PostMapping(value = "/api/test_remote_auth")
    public Mono<String> testRemoteAuth(@RequestBody @Valid Mono<String> mono) {
        String url = clubHouseConfigProperties.getApiUrl() + "/test_remote_auth";
        return webClientBuilder.build().post().uri(url).bodyValue("test").retrieve().bodyToMono(String.class);
    }
}
