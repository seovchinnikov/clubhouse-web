package edu.clubhouseapi.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

@Configuration
@ConfigurationProperties(prefix = "clubhouse")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ClubHouseConfigProperties {

    @NotNull
    private String apiUrl;

    @NotNull
    private String apiBuildId;

    @NotNull
    private String apiBuildVersion;

    @NotNull
    private String apiUa;

    @NotNull
    private String pubnubPubKey;

    @NotNull
    private String pubnubSubKey;

    @NotNull
    private String twitterId;

    @NotNull
    private String twitterSecret;

    @NotNull
    private String agoraKey;

    @NotNull
    private String sentryKey;

    @NotNull
    private String instabugKey;

    @NotNull
    private String amplitudeKey;

    @NotNull
    private String localWsAgoraUrl;

}