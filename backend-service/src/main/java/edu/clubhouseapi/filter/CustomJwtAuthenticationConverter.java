package edu.clubhouseapi.filter;

import edu.clubhouseapi.dto.SecurityUserJwtTokenBody;
import edu.clubhouseapi.jwt.JwtClientUtil;
import edu.clubhouseapi.jwt.JwtServerUtil;
import edu.clubhouseapi.service.ClubHouseAuthApiService;
import io.jsonwebtoken.ExpiredJwtException;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Component
public class CustomJwtAuthenticationConverter implements ServerAuthenticationConverter {

    private static final Logger LOG = LoggerFactory.getLogger(CustomJwtAuthenticationConverter.class);

    @Autowired
    private JwtClientUtil jwtClientUtil;

    @Autowired
    private JwtServerUtil jwtServerUtil;

    @Autowired
    private ClubHouseAuthApiService clubHouseApiService;

    @Override
    public Mono<Authentication> convert(final ServerWebExchange exchange) {
        String jwtToken = jwtClientUtil.extractJwtFromRequest(exchange.getRequest());
        if (!StringUtils.hasText(jwtToken)) {
            // no token, so no action...
            return Mono.empty();
        }
        return Mono.fromSupplier(() -> {
            return Pair.of(jwtClientUtil.parseJwtToken(jwtToken), jwtToken);
        }).onErrorResume(ExpiredJwtException.class, x -> {
            SecurityUserJwtTokenBody prevUserJwtBody = jwtClientUtil.extractFromClaims(x.getClaims());
            return Mono.just(prevUserJwtBody).flatMap(oldClubHouseToken -> {
                final Mono<String> generatedInnerToken = jwtServerUtil.generateRefreshToken(jwtToken,
                        oldClubHouseToken.getUserToken(),
                        oldClubHouseToken.getUserRefreshToken());
                return generatedInnerToken.map(newInnerToken -> {
                    final SecurityUserJwtTokenBody userJwtTokenBody = jwtClientUtil.parseJwtToken(newInnerToken);
                    exchange.getResponse().getHeaders().add("refreshtoken", newInnerToken);
                    return Pair.of(userJwtTokenBody, newInnerToken);
                });
            });
            // does not work for now
            // return clubHouseApiService.refreshToken(prevUserJwtBody).flatMap(newClubHouseToken -> {
            // if (!newClubHouseToken.getSuccess()) {
            // LOG.error("Cant obtain new token from CH! {}", newClubHouseToken.getErrorMessage());
            // return Mono.<Pair<SecurityUserJwtTokenBody, String>>empty();
            // }
            // final Mono<String> generatedInnerToken = jwtServerUtil.generateRefreshToken(jwtToken,
            // newClubHouseToken.getAccessToken(),
            // newClubHouseToken.getRefreshToken());
            // return generatedInnerToken.map(newInnerToken -> {
            // final SecurityUserJwtTokenBody userJwtTokenBody = jwtClientUtil.parseJwtToken(newInnerToken);
            // exchange.getResponse().getHeaders().add("refreshtoken", newInnerToken);
            // return Pair.of(userJwtTokenBody, newInnerToken);
            // });
            // });
        })
                .map(x -> new CustomSpringAuthenticationToken(x.getLeft().getUserId(),
                        null,
                        x.getLeft().getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()),
                        x.getLeft(),
                        x.getRight()));
    }
}