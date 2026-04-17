package ru.yandex.payments.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статус платежа")
public enum PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REFUNDED
}
