package edu.clubhouseapi.controller;

import edu.clubhouseapi.TestConfig;
import edu.clubhouseapi.common.TestAuthConst;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = TestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Configuration
@ActiveProfiles("test")
public class BaseMockTest {

    protected ClientAndServer mockServer;

    protected WebTestClient.Builder testClientBuilder;

    @BeforeEach
    public void setupMockServer() {
        mockServer = ClientAndServer.startClientAndServer(2001);
        testClientBuilder = WebTestClient.bindToServer().baseUrl("http://localhost:8080");
    }

    @AfterEach
    public void tearDownServer() {
        mockServer.stop();
    }

    public List<Header> getBasicHeaders() {
        return new ArrayList<>(TestAuthConst.getBasicHeaders());
    }

}
