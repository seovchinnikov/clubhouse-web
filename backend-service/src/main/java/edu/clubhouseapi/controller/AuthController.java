package edu.clubhouseapi.controller;

import edu.clubhouseapi.dto.CheckWaitlistedBoardedResponse;
import edu.clubhouseapi.dto.ClubHouseAuthResult;
import edu.clubhouseapi.dto.FinishPhoneNumberAuthRequest;
import edu.clubhouseapi.dto.SecurityUserJwtTokenBody;
import edu.clubhouseapi.dto.StartPhoneNumberAuthRequest;
import edu.clubhouseapi.dto.StartPhoneNumberAuthResponse;
import edu.clubhouseapi.filter.CustomSpringAuthenticationToken;
import edu.clubhouseapi.jwt.JwtServerUtil;
import edu.clubhouseapi.roles.UserRole;
import edu.clubhouseapi.service.ClubHouseAuthApiService;
import edu.clubhouseapi.service.ClubHouseStaticFilesService;
import edu.clubhouseapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

@RestController
public class AuthController {

    @Autowired
    ClubHouseAuthApiService clubHouseApiService;

    @Autowired
    JwtServerUtil jwtServerUtil;

    @Autowired
    UserService userService;

    @Autowired
    ClubHouseStaticFilesService clubHouseStaticFilesService;

    @PostMapping(value = "/api/start_phone_number_auth")
    public Mono<StartPhoneNumberAuthResponse>
            startPhoneNumberAuth(@RequestBody @Valid Mono<StartPhoneNumberAuthRequest> userMono) {
        return userMono.zipWith(userService.userInfo()).map(objects -> {
            if (objects.getT2().getPrincipal() != null) {
                throw new RuntimeException("Only for not authorized users");
            } else {
                return objects.getT1();
            }
        }).flatMap(clubHouseApiService::startPhoneNumberAuth);
    }

    @PostMapping(value = "/api/complete_phone_number_auth")
    public Mono<ClubHouseAuthResult>
            finishPhoneNumberAuth(@RequestBody @Valid Mono<FinishPhoneNumberAuthRequest> userMono) {
        return userMono.zipWith(userService.userInfo()).map(objects -> {
            if (objects.getT2().getPrincipal() != null) {
                throw new RuntimeException("Only for not authorized users");
            } else {
                return objects.getT1();
            }
        }).flatMap(clubHouseApiService::finishPhoneNumberAuth).map(x -> {
            return ClubHouseAuthResult.builder()
                    .errorMessage(x.getErrorMessage())
                    .success(x.getSuccess())
                    .userId(x.getUserId())
                    .username(x.getUsername())
                    .name(x.getName())
                    .photoUrl(clubHouseStaticFilesService.encodeUrl(x.getPhotoUrl()))
                    .userDevice(x.getDeviceId())
                    .userCookie(x.getCookie())
                    .userToken(x.getAuthToken())
                    .userRefreshToken(x.getRefreshToken())
                    .waitListed(x.getWaitListed())
                    .onboarding(x.getOnboarding())
                    .build();
        }).map(x -> {
            if (!x.getSuccess()) {
                return x;
            }
            List<String> roles = new ArrayList<>();
            roles.add(UserRole.ROLE_USER.getRoleName());
            if (!x.getWaitListed()) {
                roles.add(UserRole.ROLE_NOWAIT.getRoleName());
            }
            if (!x.getOnboarding()) {
                roles.add(UserRole.ROLE_BOARDED.getRoleName());
            }
            if (!x.getWaitListed() && !x.getOnboarding()) {
                roles.add(UserRole.ROLE_ACTIVE.getRoleName());
            }
            String token = jwtServerUtil.generateToken(x, roles);
            x.setInnerToken(token);
            return x;
        });
    }

    @PostMapping(value = "/api/check_waitlist_status")
    public Mono<CheckWaitlistedBoardedResponse> checkWaitlisted() {
        return clubHouseApiService.checkWaitlistedAndBoarded();
    }

    @GetMapping(value = "/api/user_info")
    public Mono<SecurityUserJwtTokenBody> userInfo(@RequestBody @Valid Mono<String> userInfoRequest) {
        Mono<SecurityContext> contextMono = ReactiveSecurityContextHolder.getContext();

        contextMono = contextMono.switchIfEmpty(Mono.defer(() -> Mono.just(new SecurityContextImpl())));
        return contextMono.flatMap(context -> {
            CustomSpringAuthenticationToken authentication =
                    (CustomSpringAuthenticationToken) context.getAuthentication();
            if (authentication == null
                    || authentication.getUserJwtTokenBody() == null
                    || authentication.getUserJwtTokenBody().isEmpty()) {
                return Mono.just(new SecurityUserJwtTokenBody());
            }

            return Mono.just(
                    authentication.getUserJwtTokenBody().toBuilder().success(true).userRefreshToken(null).build());
        });
    }
}
