package edu.clubhouseapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.clubhouseapi.config.ClubHouseConfigProperties;
import edu.clubhouseapi.config.MockServer;
import edu.clubhouseapi.controller.AuthController;
import edu.clubhouseapi.filter.AddAuthHeadersIfNeededFilter;
import edu.clubhouseapi.filter.CustomJwtAuthenticationConverter;
import edu.clubhouseapi.filter.JwtAuthenticationWebFilterFactory;
import edu.clubhouseapi.jwt.JwtClientUtil;
import edu.clubhouseapi.roles.UserRole;
import edu.clubhouseapi.service.ClubHouseAuthApiService;
import edu.clubhouseapi.service.UserServiceImpl;
import edu.clubhouseapi.util.ByteUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.util.HashMap;

@Configuration
@EnableWebFluxSecurity
@ComponentScan(basePackageClasses = { AuthController.class,
        JwtClientUtil.class,
        ByteUtils.class,
        ClubHouseAuthApiService.class,
        UserServiceImpl.class,
        CustomJwtAuthenticationConverter.class,
        MockServer.class })
@Import(ClubHouseConfigProperties.class)
public class ApplicationConfig {

    @Bean
    public SecurityWebFilterChain configure(ServerHttpSecurity http,
            final CustomJwtAuthenticationConverter customJwtAuthenticationConverter) {
        setUnauthorizedHandler(http);
        return http.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .addFilterBefore(webFilterJwt(customJwtAuthenticationConverter), SecurityWebFiltersOrder.AUTHENTICATION)
                .csrf()
                .disable()
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .headers()
                .frameOptions()
                .disable()
                .cache()
                .disable()
                .and()
                .authorizeExchange()
                .pathMatchers("/assets/**",
                        "/css/**",
                        "/fonts/**",
                        "/img/**",
                        "/js/**",
                        "/favicon.ico",
                        "/index.html",
                        "/")
                .permitAll()
                .pathMatchers("/api/test_auth")
                .hasAnyAuthority(UserRole.ROLE_ACTIVE.getRoleName())
                .pathMatchers("/api/test_remote_auth")
                .hasAnyAuthority(UserRole.ROLE_ACTIVE.getRoleName())
                .pathMatchers("/api/check_waitlist_status")
                .hasAnyAuthority(UserRole.ROLE_USER.getRoleName())
                .pathMatchers("/api/user_info")
                .hasAnyAuthority(UserRole.ROLE_USER.getRoleName())
                .pathMatchers("/api/start_phone_number_auth")
                .permitAll()
                .pathMatchers("/api/complete_phone_number_auth")
                .permitAll()
                .pathMatchers("/api/static/*")
                .permitAll()
                .pathMatchers("/api/*")
                .hasAnyAuthority(UserRole.ROLE_ACTIVE.getRoleName())
                .anyExchange()
                .denyAll()
                .and()
                .httpBasic()
                .disable()
                .formLogin()
                .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
                .disable()
                .logout()
                .disable()
                .build();
    }

    protected void setUnauthorizedHandler(final ServerHttpSecurity http) {
        http.exceptionHandling().authenticationEntryPoint((exchange, e) -> {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

            HashMap<String, Object> resultMap = new HashMap<>();
            resultMap.put("success", false);
            resultMap.put("error_message", "UNAUTHORIZED");
            byte[] serialized;
            try {
                serialized = new ObjectMapper().writeValueAsBytes(resultMap);
            } catch (JsonProcessingException jsonProcessingException) {
                throw new RuntimeException(e);
            }
            DefaultDataBuffer buffer = new DefaultDataBufferFactory().wrap(serialized);
            // exchange.getResponse().getHeaders().setContentLength(serialized.length);

            return exchange.getResponse().writeWith(Mono.just(buffer));
        });
    }

    public WebFilter webFilterJwt(final CustomJwtAuthenticationConverter customJwtAuthenticationConverter) {
        return JwtAuthenticationWebFilterFactory.create(customJwtAuthenticationConverter);
    }

    @Bean(name = "clubHouseWebClientBuilder")
    @Primary
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public WebClient.Builder clubHouseWebClientBuilder(ClubHouseConfigProperties clubHouseConfigProperties,
            AddAuthHeadersIfNeededFilter addAuthHeadersIfNeededFilter) {
        return WebClient.builder().exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024))
                .build()).defaultHeaders(httpHeaders -> {
            var headers = new LinkedMultiValueMap<String, String>();
            headers.setAll(new HashMap<>() {

                {
                    put("CH-Languages", "en-JP,ja-JP");
                    put("CH-Locale", "en_JP");
                    put("Accept", "application/json");
                    put("Accept-Language", "en-JP;q=1, ja-JP;q=0.9");
                    put("Accept-Encoding", "gzip, deflate");

                    put("CH-AppBuild", clubHouseConfigProperties.getApiBuildId());
                    put("CH-AppVersion", clubHouseConfigProperties.getApiBuildVersion());
                    put("User-Agent", clubHouseConfigProperties.getApiUa());
                    put("Connection", "close");
                    put("Content-Type", "application/json; charset=utf-8");

                }
            });
            httpHeaders.addAll(headers);
        }).filter(addAuthHeadersIfNeededFilter);
    }

    @Bean(name = "staticFilesWebClientBuilder")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public WebClient.Builder staticFilesWebClientBuilder(ClubHouseConfigProperties clubHouseConfigProperties,
            AddAuthHeadersIfNeededFilter addAuthHeadersIfNeededFilter) {
        return WebClient.builder().exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(1 * 1024 * 1024))
                .build()).defaultHeaders(httpHeaders -> {
            var headers = new LinkedMultiValueMap<String, String>();
            headers.setAll(new HashMap<>() {

                {
                    // put("CH-Languages", "en-JP,ja-JP");
                    // put("CH-Locale", "en_JP");
                    // put("Accept", "application/json");
                    put("Accept-Language", "ko-KR;q=1");
                    put("Accept-Encoding", "gzip, deflate, br");

                    // put("CH-AppBuild", clubHouseConfigProperties.getApiBuildId());
                    // put("CH-AppVersion", clubHouseConfigProperties.getApiBuildVersion());
                    put("User-Agent", clubHouseConfigProperties.getApiUa());
                    put("Connection", "close");
                    // put("Content-Type", "application/json; charset=utf-8");

                }
            });
            httpHeaders.addAll(headers);
        }).filter(addAuthHeadersIfNeededFilter);
    }
}
