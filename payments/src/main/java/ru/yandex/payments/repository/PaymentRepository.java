package ru.yandex.payments.repository;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.payments.model.Payment;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class PaymentRepository {

    private final Map<String, Payment> storage = new ConcurrentHashMap<>();

    public Mono<Payment> save(Payment payment) {
        if (payment.getId() == null) {
            payment.setId(UUID.randomUUID().toString());
        }
        storage.put(payment.getId(), payment);
        return Mono.just(payment);
    }

    public Mono<Payment> findById(String id) {
        return Mono.justOrEmpty(storage.get(id));
    }

    public Flux<Payment> findAll() {
        return Flux.fromIterable(storage.values());
    }

    public Flux<Payment> findByOrderId(String orderId) {
        return Flux.fromIterable(storage.values())
                .filter(payment -> payment.getOrderId().equals(orderId));
    }

    public Mono<Payment> update(Payment payment) {
        return Mono.justOrEmpty(storage.get(payment.getId()))
                .map(existing -> {
                    storage.put(payment.getId(), payment);
                    return payment;
                });
    }

    public Mono<Void> deleteById(String id) {
        return Mono.fromRunnable(() -> storage.remove(id));
    }
}
