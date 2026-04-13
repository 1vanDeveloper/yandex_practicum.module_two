package ru.yandex.practicum.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table(name = "images")
public class Image {
    @Id
    @Column("id")
    private Long id;

    @Column("item_id")
    private Long itemId;

    @Column("file_name")
    private String fileName;

    @Column("content")
    private byte[] content;
}
