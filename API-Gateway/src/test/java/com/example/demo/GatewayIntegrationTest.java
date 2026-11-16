
package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GatewayIntegrationTest {

    private final WebTestClient client = WebTestClient
            .bindToServer()
            .baseUrl("http://localhost:8080")
            .build();

    @Test
    void shouldRejectRequestWithoutToken() {
        client.get()
                .uri("/books")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldRejectInvalidToken() {
        client.get()
                .uri("/books")
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer invalid.token.123")
                .exchange()
                .expectStatus().isUnauthorized();
    }


    @Test
    void shouldAllowRequestWithValidToken() {

        String response = client.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "username": "student",
                          "password": "student123"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);

        String token = response.split("\"token\":\"")[1]
                .split("\"")[0];

        client.get()
                .uri("/books")
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + token)
                .exchange()
                .expectStatus().isOk();
    }

@Test
void shouldLoadBalanceRequestsAcrossInstances() {

    String response = client.post()
            .uri("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                    {
                      "username": "student",
                      "password": "student123"
                    }
                    """)
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(response);

    String token = response.split("\"token\":\"")[1]
            .split("\"")[0];

    Set<Integer> ports = new HashSet<>();

    for (int i = 0; i < 10; i++) {
        String body = client.get()
                .uri("/books")
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(body);

        String port = body.split("\"instancePort\":")[1]
                .split("[,}]")[0].trim();

        ports.add(Integer.parseInt(port));
    }

    org.junit.jupiter.api.Assertions.assertTrue(
            ports.contains(8082),
            "Requests should reach instance 8082");

    org.junit.jupiter.api.Assertions.assertTrue(
            ports.contains(8083),
            "Requests should reach instance 8083");
}


}
