package ru.yandex.practicum.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table(name = "cart_items")
public class CartItem {
    @Id
    @Column("id")
    private Long id;

    @Column("item_id")
    private Long itemId;

    @Column("cart_id")
    private Long cartId;

    @Column("count")
    private Integer count;

}
