package ru.yandex.practicum;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import ru.yandex.practicum.client.ApiClient;
import ru.yandex.practicum.client.api.PaymentsApi;

@Configuration
@ComponentScan(basePackages = {"ru.yandex.practicum"})
@PropertySource("classpath:application.properties")
public class WebConfiguration {

    @Bean
    public PaymentsApi paymentsApi(
            @Value("${payments.api.base-url}") String paymentsApiBaseUrl
    ) {
        ApiClient defaultClient = new ApiClient();
        defaultClient.setBasePath(paymentsApiBaseUrl);

        return new PaymentsApi(defaultClient);
    }
}
