package edu.clubhouseapi.controller;

import edu.clubhouseapi.service.ClubHouseStaticFilesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.validation.constraints.NotNull;

@RestController
public class StaticFilesController {

    @Autowired
    ClubHouseStaticFilesService clubHouseStaticFilesService;

    public static final String URL_PROXY = "/api/static";

    public static final String URL_TEMPLATE = "/api/static/{encodedUrl}";

    @GetMapping(value = URL_TEMPLATE)
    public ResponseEntity<Mono<byte[]>> getStatic(@PathVariable @NotNull String encodedUrl) {
        String decodedUrl = new String(Base64.getUrlDecoder().decode(encodedUrl), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header("Cache-Control", "max-age=31536000")
                .body(clubHouseStaticFilesService.getContent(decodedUrl));

    }
}
