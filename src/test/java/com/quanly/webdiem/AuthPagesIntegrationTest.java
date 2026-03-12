package com.quanly.webdiem;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthPagesIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void loginPageShouldLoad() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("http://localhost:" + port + "/login", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void registerPageShouldLoad() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("http://localhost:" + port + "/register", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
