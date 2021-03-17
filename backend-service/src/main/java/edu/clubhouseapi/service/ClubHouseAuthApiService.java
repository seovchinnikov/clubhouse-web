package edu.clubhouseapi.service;

import edu.clubhouseapi.config.ClubHouseConfigProperties;
import edu.clubhouseapi.dto.CheckWaitlistedBoardedResponse;
import edu.clubhouseapi.dto.FinishPhoneNumberAuthRequest;
import edu.clubhouseapi.dto.FinishPhoneNumberAuthResponse;
import edu.clubhouseapi.dto.MeRequest;
import edu.clubhouseapi.dto.MeResponse;
import edu.clubhouseapi.dto.RefreshTokenRequest;
import edu.clubhouseapi.dto.RefreshTokenResponse;
import edu.clubhouseapi.dto.SecurityUserJwtTokenBody;
import edu.clubhouseapi.dto.StartPhoneNumberAuthRequest;
import edu.clubhouseapi.dto.StartPhoneNumberAuthResponse;
import edu.clubhouseapi.filter.CustomSpringAuthenticationToken;
import edu.clubhouseapi.jwt.JwtServerUtil;
import edu.clubhouseapi.roles.UserRole;
import edu.clubhouseapi.util.ByteUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClubHouseAuthApiService {

    private static final Logger LOG = LoggerFactory.getLogger(ClubHouseAuthApiService.class);

    @Autowired
    @Qualifier("clubHouseWebClientBuilder")
    WebClient.Builder clubHouseWebClientBuilder;

    @Autowired
    ClubHouseConfigProperties clubHouseConfigProperties;

    @Autowired
    ClubHouseUserApiService clubHouseUserApiService;

    @Autowired
    ByteUtils byteUtils;

    @Autowired
    JwtServerUtil jwtServerUtil;

    public Mono<StartPhoneNumberAuthResponse>
            startPhoneNumberAuth(StartPhoneNumberAuthRequest startPhoneNumberAuthRequest) {
        String url = clubHouseConfigProperties.getApiUrl() + "/start_phone_number_auth";
        String cookie = "__cfduid=" + byteUtils.randomCookieId();
        return clubHouseWebClientBuilder.build()
                .post()
                .uri(url)
                .header("Cookie", cookie)
                .header("CH-DeviceId", byteUtils.fixedUuidFromString(cookie))
                .bodyValue(startPhoneNumberAuthRequest)
                .retrieve()
                .bodyToMono(StartPhoneNumberAuthResponse.class)
                .map(x -> {
                    x.setCookie(cookie);
                    return x;
                })
                .switchIfEmpty(Mono.defer(() -> Mono
                        .just(StartPhoneNumberAuthResponse.builder().success(false).errorMessage("Empty").build())));
    }

    public Mono<FinishPhoneNumberAuthResponse>
            finishPhoneNumberAuth(FinishPhoneNumberAuthRequest finishPhoneNumberAuthRequest) {
        String url = clubHouseConfigProperties.getApiUrl() + "/complete_phone_number_auth";
        String cookie = finishPhoneNumberAuthRequest.getCookie();
        finishPhoneNumberAuthRequest.setCookie(null);
        return clubHouseWebClientBuilder.build()
                .post()
                .uri(url)
                .header("Cookie", cookie)
                .header("CH-DeviceId", byteUtils.fixedUuidFromString(cookie))
                .bodyValue(finishPhoneNumberAuthRequest)
                .retrieve()
                .bodyToMono(FinishPhoneNumberAuthResponse.class)
                .map(x -> {
                    // LOG.warn("finish response:" + x.toString());
                    x.setCookie(cookie);
                    x.setDeviceId(byteUtils.fixedUuidFromString(cookie));
                    return x;
                })
                .switchIfEmpty(Mono.defer(() -> Mono
                        .just(FinishPhoneNumberAuthResponse.builder().success(false).errorMessage("Empty").build())));
    }

    public Mono<CheckWaitlistedBoardedResponse> checkWaitlistedAndBoarded() {
        String url = clubHouseConfigProperties.getApiUrl() + "/check_waitlist_status";

        Mono<SecurityContext> contextMono = ReactiveSecurityContextHolder.getContext();

        return contextMono.flatMap(context -> {
            CustomSpringAuthenticationToken authentication =
                    (CustomSpringAuthenticationToken) context.getAuthentication();
            if (authentication == null
                    || authentication.getUserJwtTokenBody() == null
                    || authentication.getUserJwtTokenBody().isEmpty()) {
                return Mono.error(new IllegalArgumentException("cant be here"));
            }

            Mono<MeResponse> meInfo =
                    clubHouseUserApiService.me(MeRequest.builder().timezoneIdentifier("Asia/Tokyo").build());
            return clubHouseWebClientBuilder.build()
                    .post()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(CheckWaitlistedBoardedResponse.class)
                    .zipWith(meInfo)
                    .map(zipped -> {
                        CheckWaitlistedBoardedResponse waitListResult = zipped.getT1();
                        MeResponse meResponse = zipped.getT2();
                        if (!waitListResult.getSuccess()) {
                            return waitListResult;
                        }
                        boolean waitListed =
                                waitListResult.getIsWaitlisted() == null || waitListResult.getIsWaitlisted();
                        boolean boarded = StringUtils.isNotEmpty(meResponse.getUserProfile().getUsername());

                        SecurityUserJwtTokenBody.SecurityUserJwtTokenBodyBuilder<?, ?> securityUserJwtTokenBodyBuilder =
                                authentication.getUserJwtTokenBody().toBuilder();
                        List<String> roles = new ArrayList<>(authentication.getUserJwtTokenBody().getRoles());

                        if (!waitListed && !roles.contains(UserRole.ROLE_NOWAIT.getRoleName())) {
                            roles.add(UserRole.ROLE_NOWAIT.getRoleName());
                        }
                        if (boarded && !roles.contains(UserRole.ROLE_BOARDED.getRoleName())) {
                            roles.add(UserRole.ROLE_BOARDED.getRoleName());
                        }

                        if (roles.contains(UserRole.ROLE_BOARDED.getRoleName())
                                && roles.contains(UserRole.ROLE_NOWAIT.getRoleName())) {
                            roles.add(UserRole.ROLE_ACTIVE.getRoleName());
                        }
                        SecurityUserJwtTokenBody newJwt = securityUserJwtTokenBodyBuilder.roles(roles).build();
                        String tokenString = jwtServerUtil.generateToken(newJwt);
                        return CheckWaitlistedBoardedResponse.builder()
                                .innerToken(tokenString)
                                .isWaitlisted(waitListed)
                                .isBoarded(boarded)
                                .success(true)
                                .securityUserJwtTokenBody(newJwt.toBuilder().userRefreshToken(null).build())
                                .build();

                    })
                    .switchIfEmpty(Mono.defer(() -> Mono.just(
                            CheckWaitlistedBoardedResponse.builder().success(false).errorMessage("Empty").build())));

        });

    }

    public Mono<RefreshTokenResponse> refreshToken(final SecurityUserJwtTokenBody prevUserJwtBody) {
        String url = clubHouseConfigProperties.getApiUrl() + "/refresh_token";
        return clubHouseWebClientBuilder.build()
                .post()
                .uri(url)
                .header("Cookie", prevUserJwtBody.getUserCookie())
                .header("CH-UserID", prevUserJwtBody.getUserId())
                .header("CH-DeviceId", prevUserJwtBody.getUserDevice())
                .header("Authorization", prevUserJwtBody.getUserToken())
                .bodyValue(RefreshTokenRequest.builder().refreshToken(prevUserJwtBody.getUserRefreshToken()).build())
                .retrieve()
                .bodyToMono(RefreshTokenResponse.class)
                .switchIfEmpty(Mono.defer(
                        () -> Mono.just(RefreshTokenResponse.builder().success(false).errorMessage("Empty").build())));

    }
}
