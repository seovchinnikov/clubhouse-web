package edu.clubhouseapi.filter;

import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

import java.util.Collections;

public class CustomReactiveJwtAuthenticationManager extends UserDetailsRepositoryReactiveAuthenticationManager {

    public CustomReactiveJwtAuthenticationManager() {
        // just disable it
        super(new MapReactiveUserDetailsService(
                Collections.singletonList(new User("empty", "empty", Collections.emptyList()))));
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        if (authentication.isAuthenticated()) {
            // we'll be always here
            return Mono.just(authentication);
        } else {
            // not our case...
            throw new UnsupportedOperationException("User repo is disabled for JWT auth!");
        }
    }

    @Override
    protected Mono<UserDetails> retrieveUser(String username) {
        throw new UnsupportedOperationException("User repo is disabled for JWT auth!");
    }
}
