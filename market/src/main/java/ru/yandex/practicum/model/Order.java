package ru.yandex.practicum.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table(name = "orders")
public class Order {
    @Id
    @Column("id")
    private Long id;

    @Column("user_id")
    private Long userId;

}
