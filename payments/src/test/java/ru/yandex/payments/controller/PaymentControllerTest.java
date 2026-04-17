package ru.yandex.payments.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.payments.model.CreatePaymentRequest;
import ru.yandex.payments.model.Payment;
import ru.yandex.payments.model.PaymentStatus;
import ru.yandex.payments.service.PaymentService;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureWebTestClient
class PaymentControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private PaymentService paymentService;

    private Payment testPayment;
    private CreatePaymentRequest testRequest;

    @BeforeEach
    void setUp() {
        testPayment = new Payment(
                "payment-1",
                "order-123",
                new BigDecimal("100.00"),
                "USD",
                PaymentStatus.PENDING
        );
        testPayment.setCreatedAt(Instant.now());
        testPayment.setUpdatedAt(Instant.now());

        testRequest = new CreatePaymentRequest(
                "order-123",
                new BigDecimal("100.00"),
                "USD"
        );
    }

    @Test
    void createPayment_validRequest_returnsCreatedPayment() {
        when(paymentService.createPayment(any(CreatePaymentRequest.class)))
                .thenReturn(Mono.just(testPayment));

        webTestClient.post()
                .uri("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(testRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo("payment-1")
                .jsonPath("$.orderId").isEqualTo("order-123")
                .jsonPath("$.amount").isEqualTo(100.00)
                .jsonPath("$.currency").isEqualTo("USD")
                .jsonPath("$.status").isEqualTo("PENDING");
    }

    @Test
    void createPayment_invalidRequest_missingOrderId_returnsBadRequest() {
        CreatePaymentRequest invalidRequest = new CreatePaymentRequest(
                null,
                new BigDecimal("100.00"),
                "USD"
        );

        webTestClient.post()
                .uri("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createPayment_invalidRequest_negativeAmount_returnsBadRequest() {
        CreatePaymentRequest invalidRequest = new CreatePaymentRequest(
                "order-123",
                new BigDecimal("-100.00"),
                "USD"
        );

        webTestClient.post()
                .uri("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createPayment_invalidRequest_zeroAmount_returnsBadRequest() {
        CreatePaymentRequest invalidRequest = new CreatePaymentRequest(
                "order-123",
                BigDecimal.ZERO,
                "USD"
        );

        webTestClient.post()
                .uri("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createPayment_invalidRequest_missingCurrency_returnsBadRequest() {
        CreatePaymentRequest invalidRequest = new CreatePaymentRequest(
                "order-123",
                new BigDecimal("100.00"),
                null
        );

        webTestClient.post()
                .uri("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getPaymentById_existingId_returnsPayment() {
        when(paymentService.getPaymentById("payment-1"))
                .thenReturn(Mono.just(testPayment));

        webTestClient.get()
                .uri("/api/payments/payment-1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo("payment-1")
                .jsonPath("$.orderId").isEqualTo("order-123")
                .jsonPath("$.amount").isEqualTo(100.00)
                .jsonPath("$.status").isEqualTo("PENDING");
    }

    @Test
    void getPaymentById_nonExistingId_returnsNotFound() {
        when(paymentService.getPaymentById("non-existing"))
                .thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/payments/non-existing")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getAllPayments_returnsFluxOfPayments() {
        Payment payment2 = new Payment(
                "payment-2",
                "order-456",
                new BigDecimal("200.00"),
                "EUR",
                PaymentStatus.COMPLETED
        );

        when(paymentService.getAllPayments())
                .thenReturn(Flux.just(testPayment, payment2));

        webTestClient.get()
                .uri("/api/payments")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[?(@.id=='payment-1')].orderId").isEqualTo("order-123")
                .jsonPath("$[?(@.id=='payment-2')].orderId").isEqualTo("order-456");
    }

    @Test
    void getAllPayments_emptyList_returnsEmptyFlux() {
        when(paymentService.getAllPayments())
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/payments")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Payment.class)
                .hasSize(0);
    }

    @Test
    void getPaymentsByOrderId_existingOrderId_returnsPayments() {
        Payment payment2 = new Payment(
                "payment-2",
                "order-123",
                new BigDecimal("50.00"),
                "USD",
                PaymentStatus.COMPLETED
        );

        when(paymentService.getPaymentsByOrderId("order-123"))
                .thenReturn(Flux.just(testPayment, payment2));

        webTestClient.get()
                .uri("/api/payments/order/order-123")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Payment.class)
                .hasSize(2);
    }

    @Test
    void getPaymentsByOrderId_nonExistingOrderId_returnsEmptyFlux() {
        when(paymentService.getPaymentsByOrderId("non-existing-order"))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/payments/order/non-existing-order")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Payment.class)
                .hasSize(0);
    }

    @Test
    void processPayment_existingPendingPayment_returnsProcessedPayment() {
        Payment processedPayment = new Payment(
                "payment-1",
                "order-123",
                new BigDecimal("100.00"),
                "USD",
                PaymentStatus.COMPLETED
        );

        when(paymentService.processPayment("payment-1"))
                .thenReturn(Mono.just(processedPayment));

        webTestClient.post()
                .uri("/api/payments/payment-1/process")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo("payment-1")
                .jsonPath("$.status").isEqualTo("COMPLETED");
    }

    @Test
    void processPayment_nonExistingPayment_returnsNotFound() {
        when(paymentService.processPayment("non-existing"))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/api/payments/non-existing/process")
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();
    }

    @Test
    void processPayment_invalidState_returnsBadRequest() {
        when(paymentService.processPayment("payment-1"))
                .thenReturn(Mono.error(new IllegalStateException("Payment cannot be processed: COMPLETED")));

        webTestClient.post()
                .uri("/api/payments/payment-1/process")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void cancelPayment_existingPayment_returnsCancelledPayment() {
        Payment cancelledPayment = new Payment(
                "payment-1",
                "order-123",
                new BigDecimal("100.00"),
                "USD",
                PaymentStatus.FAILED
        );

        when(paymentService.cancelPayment("payment-1"))
                .thenReturn(Mono.just(cancelledPayment));

        webTestClient.post()
                .uri("/api/payments/payment-1/cancel")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo("payment-1")
                .jsonPath("$.status").isEqualTo("FAILED");
    }

    @Test
    void cancelPayment_nonExistingPayment_returnsNotFound() {
        when(paymentService.cancelPayment("non-existing"))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/api/payments/non-existing/cancel")
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();
    }

    @Test
    void cancelPayment_invalidState_returnsBadRequest() {
        when(paymentService.cancelPayment("payment-1"))
                .thenReturn(Mono.error(new IllegalStateException("Cannot cancel completed payment")));

        webTestClient.post()
                .uri("/api/payments/payment-1/cancel")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void refundPayment_existingCompletedPayment_returnsRefundedPayment() {
        Payment refundedPayment = new Payment(
                "payment-1",
                "order-123",
                new BigDecimal("100.00"),
                "USD",
                PaymentStatus.REFUNDED
        );

        when(paymentService.refundPayment("payment-1"))
                .thenReturn(Mono.just(refundedPayment));

        webTestClient.post()
                .uri("/api/payments/payment-1/refund")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo("payment-1")
                .jsonPath("$.status").isEqualTo("REFUNDED");
    }

    @Test
    void refundPayment_nonExistingPayment_returnsNotFound() {
        when(paymentService.refundPayment("non-existing"))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/api/payments/non-existing/refund")
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();
    }

    @Test
    void refundPayment_invalidState_returnsBadRequest() {
        when(paymentService.refundPayment("payment-1"))
                .thenReturn(Mono.error(new IllegalStateException("Can only refund completed payments")));

        webTestClient.post()
                .uri("/api/payments/payment-1/refund")
                .exchange()
                .expectStatus().isBadRequest();
    }
}
