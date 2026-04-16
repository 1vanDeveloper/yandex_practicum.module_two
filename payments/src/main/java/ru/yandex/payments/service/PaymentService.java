package ru.yandex.payments.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.payments.model.CreatePaymentRequest;
import ru.yandex.payments.model.Payment;
import ru.yandex.payments.model.PaymentStatus;
import ru.yandex.payments.repository.PaymentRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Mono<Payment> createPayment(CreatePaymentRequest request) {
        Payment payment = new Payment(
                null,
                request.getOrderId(),
                request.getAmount(),
                request.getCurrency().toUpperCase(),
                PaymentStatus.PENDING
        );
        return paymentRepository.save(payment);
    }

    public Mono<Payment> getPaymentById(String id) {
        return paymentRepository.findById(id);
    }

    public Flux<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Flux<Payment> getPaymentsByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    public Mono<Payment> processPayment(String id) {
        return paymentRepository.findById(id)
                .flatMap(payment -> {
                    if (payment.getStatus() != PaymentStatus.PENDING) {
                        return Mono.error(new IllegalStateException("Payment cannot be processed: " + payment.getStatus()));
                    }
                    payment.setStatus(PaymentStatus.PROCESSING);
                    return paymentRepository.update(payment);
                })
                .flatMap(payment -> {
                    payment.setStatus(PaymentStatus.COMPLETED);
                    return paymentRepository.update(payment);
                });
    }

    public Mono<Payment> cancelPayment(String id) {
        return paymentRepository.findById(id)
                .flatMap(payment -> {
                    if (payment.getStatus() == PaymentStatus.COMPLETED) {
                        return Mono.error(new IllegalStateException("Cannot cancel completed payment"));
                    }
                    payment.setStatus(PaymentStatus.FAILED);
                    return paymentRepository.update(payment);
                });
    }

    public Mono<Payment> refundPayment(String id) {
        return paymentRepository.findById(id)
                .flatMap(payment -> {
                    if (payment.getStatus() != PaymentStatus.COMPLETED) {
                        return Mono.error(new IllegalStateException("Can only refund completed payments"));
                    }
                    payment.setStatus(PaymentStatus.REFUNDED);
                    return paymentRepository.update(payment);
                });
    }
}
