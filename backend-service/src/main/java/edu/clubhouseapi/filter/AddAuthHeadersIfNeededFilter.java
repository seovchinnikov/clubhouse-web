package edu.clubhouseapi.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

@Component
public class AddAuthHeadersIfNeededFilter implements ExchangeFilterFunction {

    private static final Logger LOG = LoggerFactory.getLogger(AddAuthHeadersIfNeededFilter.class);

    @Override
    public Mono<ClientResponse> filter(final ClientRequest clientRequest, final ExchangeFunction next) {
        Mono<SecurityContext> contextMono = ReactiveSecurityContextHolder.getContext();

        contextMono = contextMono.switchIfEmpty(Mono.defer(() -> {
            LOG.warn("http headers unauth, " + clientRequest.url().toString() + " " + clientRequest.headers().toString());
            return Mono.just(new SecurityContextImpl());
        }));
        Mono<ClientRequest> authorization = contextMono.map(context -> {
            CustomSpringAuthenticationToken authentication =
                    (CustomSpringAuthenticationToken) context.getAuthentication();
            if (authentication == null
                    || authentication.getUserJwtTokenBody() == null
                    || authentication.getUserJwtTokenBody().isEmpty()
                            && !clientRequest.headers().containsKey("CH-UserID")) {
                ClientRequest.Builder newRequest = ClientRequest.from(clientRequest);
                newRequest.header("CH-UserID", "(null)");
                return newRequest.build();
            }
            ClientRequest.Builder newRequest = ClientRequest.from(clientRequest);
            if (!clientRequest.headers().containsKey("CH-UserID")) {
                newRequest.header("CH-UserID", authentication.getUserJwtTokenBody().getUserId());
            }
            if (!clientRequest.headers().containsKey("CH-DeviceId")) {
                newRequest.header("CH-DeviceId", authentication.getUserJwtTokenBody().getUserDevice());
            }
            if (!clientRequest.headers().containsKey("Authorization")) {
                newRequest.header("Authorization", "Token " + authentication.getUserJwtTokenBody().getUserToken());
            }
            if (!clientRequest.headers().containsKey("Cookie")) {
                newRequest.header("Cookie", authentication.getUserJwtTokenBody().getUserCookie());
            }

            // newRequest.headers(httpHeaders -> {
            // LOG.warn("http headers auth, " + clientRequest.url().toString() + " " + httpHeaders.toString());
            // });

            return newRequest.build();
        });
        authorization = authorization.switchIfEmpty(Mono.defer(() -> Mono.just(clientRequest)));
        return authorization.flatMap(next::exchange);
    }

}
