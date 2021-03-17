package edu.clubhouseapi.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

@Service
public class ClassPathUtils {

    public String getResourceAsString(String resource, Charset charset) {
        try {
            try (InputStream inputStream = new ClassPathResource(resource).getInputStream()) {
                return StreamUtils.copyToString(inputStream, charset);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
