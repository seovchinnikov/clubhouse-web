package edu.clubhouseapi.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;

public class JwtAuthenticationWebFilterFactory {

    private static final Logger LOG = LoggerFactory.getLogger(JwtAuthenticationWebFilterFactory.class);

    public static AuthenticationWebFilter create(CustomJwtAuthenticationConverter customJwtAuthenticationConverter,
            ReactiveAuthenticationManager authenticationManager) {
        final AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(authenticationManager);
        authenticationWebFilter.setServerAuthenticationConverter(customJwtAuthenticationConverter);
        authenticationWebFilter.setAuthenticationFailureHandler(responseError());
        return authenticationWebFilter;
    }

    public static AuthenticationWebFilter create(CustomJwtAuthenticationConverter customJwtAuthenticationConverter) {
        final AuthenticationWebFilter authenticationWebFilter =
                new AuthenticationWebFilter(new CustomReactiveJwtAuthenticationManager());
        authenticationWebFilter.setServerAuthenticationConverter(customJwtAuthenticationConverter);
        authenticationWebFilter.setAuthenticationFailureHandler(responseError());
        return authenticationWebFilter;
    }

    protected static ServerAuthenticationFailureHandler responseError() {
        return (webFilterExchange, exception) -> {
            LOG.error("error during AuthenticationWebFilter", exception);
            webFilterExchange.getExchange().getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            // webFilterExchange.getExchange()
            // .getResponse()
            // .getHeaders()
            // .add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            // webFilterExchange.getExchange()
            // .getResponse()
            // .writeWith(Mono.just(new DefaultDataBufferFactory().wrap(SerializationUtils
            // .serialize("{\"success\": false, \"error_message\": \"UNAUTHORIZED\"}"))));
            return webFilterExchange.getExchange().getResponse().setComplete();
        };
    }
}
