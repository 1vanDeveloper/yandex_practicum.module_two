package ru.yandex.payments.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Информация о платеже")
public class Payment {

    @Schema(description = "Идентификатор платежа", example = "payment-123")
    private String id;
    @Schema(description = "Идентификатор заказа", example = "order-456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderId;
    @Schema(description = "Сумма платежа", example = "100.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;
    @Schema(description = "Валюта платежа (ISO 4217)", example = "USD", requiredMode = Schema.RequiredMode.REQUIRED)
    private String currency;
    @Schema(description = "Статус платежа", example = "PENDING", requiredMode = Schema.RequiredMode.REQUIRED)
    private PaymentStatus status;
    @Schema(description = "Дата и время создания платежа")
    private Instant createdAt;
    @Schema(description = "Дата и время последнего обновления")
    private Instant updatedAt;

    public Payment() {
    }

    public Payment(String id, String orderId, BigDecimal amount, String currency, PaymentStatus status) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
