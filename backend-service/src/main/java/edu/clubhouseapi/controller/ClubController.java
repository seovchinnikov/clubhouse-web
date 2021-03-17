package edu.clubhouseapi.controller;

import edu.clubhouseapi.dto.GetClubRequest;
import edu.clubhouseapi.dto.GetClubResponse;
import edu.clubhouseapi.service.ClubHouseClubsApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
public class ClubController {

    @Autowired
    ClubHouseClubsApiService clubHouseClubsApiService;

    @PostMapping(value = "/api/get_club")
    public Mono<GetClubResponse> getClub(@RequestBody @Valid Mono<GetClubRequest> mono) {
        return mono.flatMap(x -> clubHouseClubsApiService.getClub(x));
    }
}
