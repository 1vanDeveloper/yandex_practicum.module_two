package ru.yandex.practicum.config;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

@Component
public class OAuth2ClientCredentialsFilter implements ExchangeFilterFunction {

    private final KeycloakTokenService tokenService;

    public OAuth2ClientCredentialsFilter(KeycloakTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        return tokenService.getAccessToken()
                .flatMap(token -> {
                    ClientRequest filteredRequest = ClientRequest.from(request)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .build();
                    return next.exchange(filteredRequest);
                });
    }
}
