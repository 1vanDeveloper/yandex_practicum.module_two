package ru.yandex.practicum;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.client.ApiClient;
import ru.yandex.practicum.client.api.PaymentsApi;

@Configuration
@PropertySource("classpath:application.properties")
public class WebConfiguration {

    @Bean
    public WebClient paymentsWebClient(ObjectProvider<ServerOAuth2AuthorizedClientExchangeFilterFunction> oauth2FilterProvider) {
        WebClient.Builder builder = WebClient.builder();
        oauth2FilterProvider.ifAvailable(builder::filter);
        return builder.build();
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public PaymentsApi paymentsApi(
            @Value("${payments.api.base-url}") String paymentsApiBaseUrl,
            WebClient paymentsWebClient
    ) {
        ApiClient defaultClient = new ApiClient(paymentsWebClient);
        defaultClient.setBasePath(paymentsApiBaseUrl);

        return new PaymentsApi(defaultClient);
    }
}
