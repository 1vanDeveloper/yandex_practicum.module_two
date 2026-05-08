package ru.yandex.practicum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Service
public class KeycloakTokenService {

    private final WebClient webClient;
    private final String tokenUri;
    private final String clientId;
    private final String clientSecret;

    private volatile String cachedToken;
    private volatile Instant tokenExpiry;

    public KeycloakTokenService(
            @Value("${keycloak.token-uri}") String tokenUri,
            @Value("${keycloak.client-id}") String clientId,
            @Value("${keycloak.client-secret}") String clientSecret) {
        
        this.tokenUri = tokenUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.webClient = WebClient.builder().build();
    }

    public Mono<String> getAccessToken() {
        if (cachedToken != null && tokenExpiry != null && Instant.now().isBefore(tokenExpiry)) {
            return Mono.just(cachedToken);
        }
        return requestNewToken();
    }

    private Mono<String> requestNewToken() {
        return webClient.post()
                .uri(tokenUri)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue(
                        "grant_type=client_credentials" +
                                "&client_id=" + clientId +
                                "&client_secret=" + clientSecret
                )
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .doOnNext(response -> {
                    this.cachedToken = response.getAccessToken();
                    this.tokenExpiry = Instant.now().plusSeconds(response.getExpiresIn() - 60);
                })
                .map(TokenResponse::getAccessToken);
    }

    private static class TokenResponse {
        private String access_token;
        private long expires_in;

        public String getAccessToken() {
            return access_token;
        }

        public void setAccessToken(String access_token) {
            this.access_token = access_token;
        }

        public long getExpiresIn() {
            return expires_in;
        }

        public void setExpiresIn(long expires_in) {
            this.expires_in = expires_in;
        }
    }
}
