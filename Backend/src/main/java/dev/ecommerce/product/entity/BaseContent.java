package dev.ecommerce.product.entity;

import dev.ecommerce.product.constant.ContentType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseContent {

    @Setter(AccessLevel.NONE)
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Enumerated(EnumType.STRING)
    protected ContentType contentType;

    @Column(columnDefinition = "TEXT")
    protected String content;

    protected Integer sortOrder;
}
