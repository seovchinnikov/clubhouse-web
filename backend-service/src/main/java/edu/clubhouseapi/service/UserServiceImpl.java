package edu.clubhouseapi.service;

import edu.clubhouseapi.filter.CustomSpringAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserServiceImpl implements UserService {

    @Override
    public Mono<CustomSpringAuthenticationToken> userInfo() {

        Mono<SecurityContext> contextMono = ReactiveSecurityContextHolder.getContext();

        return contextMono.flatMap(context -> {
            CustomSpringAuthenticationToken authentication =
                    (CustomSpringAuthenticationToken) context.getAuthentication();
            if (authentication == null
                    || authentication.getUserJwtTokenBody() == null
                    || authentication.getUserJwtTokenBody().isEmpty()) {
                return Mono.empty();
            }

            return Mono.just(authentication);
        }).switchIfEmpty(Mono.defer(() -> Mono.just(new CustomSpringAuthenticationToken(null, null, null, null))));
    }
}
