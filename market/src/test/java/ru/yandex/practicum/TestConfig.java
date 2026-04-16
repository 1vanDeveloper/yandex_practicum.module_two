package ru.yandex.practicum;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.client.api.PaymentsApi;
import ru.yandex.practicum.client.model.CreatePaymentRequest;
import ru.yandex.practicum.client.model.Payment;
import reactor.core.publisher.Mono;

@TestConfiguration
@Import(WebConfiguration.class)
public class TestConfig {

    @Bean
    public PaymentsApi paymentsApi() {
        return new PaymentsApi() {
            @Override
            public Mono<Payment> createPayment(CreatePaymentRequest request) {
                Payment payment = new Payment();
                payment.setId("payment-" + request.getOrderId());
                payment.setOrderId(request.getOrderId());
                payment.setAmount(request.getAmount());
                payment.setCurrency(request.getCurrency());
                payment.setStatus(Payment.StatusEnum.PENDING);
                return Mono.just(payment);
            }
        };
    }
}
