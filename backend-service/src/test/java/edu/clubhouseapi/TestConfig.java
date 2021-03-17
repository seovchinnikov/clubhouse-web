package edu.clubhouseapi;

import edu.clubhouseapi.util.ByteUtils;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import({ ApplicationConfig.class })
public class TestConfig {

    @SpyBean
    protected ByteUtils byteUtils;

}
