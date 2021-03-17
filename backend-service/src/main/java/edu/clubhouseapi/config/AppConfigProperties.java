package edu.clubhouseapi.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AppConfigProperties {

    private boolean mockEnable = false;

    private String mockCookie = "__cfduid=d7949e3801f62c89d21196ed4d076990958db24bfa6";

    private int mockPort = 2002;
}