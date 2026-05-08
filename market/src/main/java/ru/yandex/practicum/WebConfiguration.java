package ru.yandex.practicum;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.client.ApiClient;
import ru.yandex.practicum.client.api.PaymentsApi;
import ru.yandex.practicum.config.OAuth2ClientCredentialsFilter;

@Configuration
@PropertySource("classpath:application.properties")
public class WebConfiguration {

    @Bean
    public WebClient paymentsWebClient(OAuth2ClientCredentialsFilter oauth2Filter) {
        return WebClient.builder()
                .filter(oauth2Filter)
                .build();
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
