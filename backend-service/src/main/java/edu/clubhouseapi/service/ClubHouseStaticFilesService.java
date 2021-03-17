package edu.clubhouseapi.service;

import edu.clubhouseapi.config.ClubHouseConfigProperties;
import edu.clubhouseapi.controller.StaticFilesController;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class ClubHouseStaticFilesService {

    @Autowired
    @Qualifier("staticFilesWebClientBuilder")
    WebClient.Builder staticFilesWebClientBuilder;

    @Autowired
    ClubHouseConfigProperties clubHouseConfigProperties;

    public Mono<byte[]> getContent(String url) {
        return staticFilesWebClientBuilder.build().get().uri(url).retrieve().bodyToMono(byte[].class);
    }

    public String encodeUrl(String url) {
        if (StringUtils.isEmpty(url)) {
            return url;
        }
        return StaticFilesController.URL_PROXY
                + "/"
                + Base64.getUrlEncoder().encodeToString(url.getBytes(StandardCharsets.UTF_8));
    }
}
