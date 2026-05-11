package ru.yandex.practicum.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.ClientCredentialsReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository;

@Configuration
@ConditionalOnProperty(prefix = "spring.security.oauth2.client.registration.payments", name = "client-id")
public class OAuth2ClientConfig {

    @Bean
    public ServerOAuth2AuthorizedClientRepository authorizedClientRepository() {
        return new WebSessionServerOAuth2AuthorizedClientRepository();
    }

    @Bean
    public ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider() {
        return new ClientCredentialsReactiveOAuth2AuthorizedClientProvider();
    }

    @Bean
    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
            ReactiveClientRegistrationRepository clientRegistrationRepository,
            ServerOAuth2AuthorizedClientRepository authorizedClientRepository,
            ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider) {
        
        DefaultReactiveOAuth2AuthorizedClientManager manager = 
            new DefaultReactiveOAuth2AuthorizedClientManager(
                clientRegistrationRepository, 
                authorizedClientRepository
            );
        manager.setAuthorizedClientProvider(authorizedClientProvider);
        
        return manager;
    }

    @Bean
    public ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2Filter(
            ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction filter = 
            new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        filter.setDefaultClientRegistrationId("payments");
        return filter;
    }
}
