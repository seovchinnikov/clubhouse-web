package edu.clubhouseapi.service;

import edu.clubhouseapi.filter.CustomSpringAuthenticationToken;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<CustomSpringAuthenticationToken> userInfo();
}
