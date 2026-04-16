package ru.yandex.payments.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.payments.model.CreatePaymentRequest;
import ru.yandex.payments.model.Payment;
import ru.yandex.payments.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Payment> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return paymentService.createPayment(request);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Payment>> getPaymentById(@PathVariable String id) {
        return paymentService.getPaymentById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Flux<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @GetMapping("/order/{orderId}")
    public Flux<Payment> getPaymentsByOrderId(@PathVariable String orderId) {
        return paymentService.getPaymentsByOrderId(orderId);
    }

    @PostMapping("/{id}/process")
    public Mono<ResponseEntity<Payment>> processPayment(@PathVariable String id) {
        return paymentService.processPayment(id)
                .map(ResponseEntity::ok)
                .onErrorResume(IllegalStateException.class, e -> 
                    Mono.just(ResponseEntity.badRequest().build()));
    }

    @PostMapping("/{id}/cancel")
    public Mono<ResponseEntity<Payment>> cancelPayment(@PathVariable String id) {
        return paymentService.cancelPayment(id)
                .map(ResponseEntity::ok)
                .onErrorResume(IllegalStateException.class, e -> 
                    Mono.just(ResponseEntity.badRequest().build()));
    }

    @PostMapping("/{id}/refund")
    public Mono<ResponseEntity<Payment>> refundPayment(@PathVariable String id) {
        return paymentService.refundPayment(id)
                .map(ResponseEntity::ok)
                .onErrorResume(IllegalStateException.class, e -> 
                    Mono.just(ResponseEntity.badRequest().build()));
    }
}
