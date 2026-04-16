package ru.yandex.payments.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Payments", description = "API для управления платежами")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать новый платеж", description = "Создаёт новый платёж с указанными параметрами")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Платёж успешно создан"),
        @ApiResponse(responseCode = "400", description = "Некорректные данные запроса")
    })
    public Mono<Payment> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return paymentService.createPayment(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить платёж по ID", description = "Возвращает информацию о платеже по его идентификатору")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Платёж найден"),
        @ApiResponse(responseCode = "404", description = "Платёж не найден")
    })
    public Mono<ResponseEntity<Payment>> getPaymentById(
            @Parameter(description = "Идентификатор платежа", required = true)
            @PathVariable String id) {
        return paymentService.getPaymentById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Получить все платежи", description = "Возвращает список всех платежей")
    @ApiResponse(responseCode = "200", description = "Список платежей получен успешно")
    public Flux<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Получить платежи по заказу", description = "Возвращает список платежей для указанного заказа")
    @ApiResponse(responseCode = "200", description = "Список платежей получен успешно")
    public Flux<Payment> getPaymentsByOrderId(
            @Parameter(description = "Идентификатор заказа", required = true)
            @PathVariable String orderId) {
        return paymentService.getPaymentsByOrderId(orderId);
    }

    @PostMapping("/{id}/process")
    @Operation(summary = "Обработать платёж", description = "Запускает процесс обработки платежа")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Платёж успешно обработан"),
        @ApiResponse(responseCode = "400", description = "Платёж не может быть обработан"),
        @ApiResponse(responseCode = "404", description = "Платёж не найден")
    })
    public Mono<ResponseEntity<Payment>> processPayment(
            @Parameter(description = "Идентификатор платежа", required = true)
            @PathVariable String id) {
        return paymentService.processPayment(id)
                .map(ResponseEntity::ok)
                .onErrorResume(IllegalStateException.class, e ->
                    Mono.just(ResponseEntity.badRequest().build()));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Отменить платёж", description = "Отменяет ожидающий или обрабатываемый платёж")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Платёж успешно отменён"),
        @ApiResponse(responseCode = "400", description = "Платёж не может быть отменён"),
        @ApiResponse(responseCode = "404", description = "Платёж не найден")
    })
    public Mono<ResponseEntity<Payment>> cancelPayment(
            @Parameter(description = "Идентификатор платежа", required = true)
            @PathVariable String id) {
        return paymentService.cancelPayment(id)
                .map(ResponseEntity::ok)
                .onErrorResume(IllegalStateException.class, e ->
                    Mono.just(ResponseEntity.badRequest().build()));
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Вернуть платёж", description = "Возвращает средства по завершённому платежу")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Платёж успешно возвращён"),
        @ApiResponse(responseCode = "400", description = "Платёж не может быть возвращён"),
        @ApiResponse(responseCode = "404", description = "Платёж не найден")
    })
    public Mono<ResponseEntity<Payment>> refundPayment(
            @Parameter(description = "Идентификатор платежа", required = true)
            @PathVariable String id) {
        return paymentService.refundPayment(id)
                .map(ResponseEntity::ok)
                .onErrorResume(IllegalStateException.class, e ->
                    Mono.just(ResponseEntity.badRequest().build()));
    }
}
