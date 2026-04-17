package ru.yandex.practicum.model;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Getter
@Setter
@Table(name = "order_items")
public class OrderItem {
    @Id
    @Column("id")
    private Long id;

    @Column("item_id")
    private Long itemId;

    @Column("order_id")
    private Long orderId;

    @Size(max = 256)
    @Column("title")
    private String title;

    @Column("price")
    private BigDecimal price;

    @Column("count")
    private Integer count;

}
