package com.ihome24.ihome24.entity.product;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * B-4: 301 с old_slug на актуальный new_slug (без цепочек — схлопываются при записи).
 */
@Entity
@Table(name = "product_slug_redirects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSlugRedirect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "old_slug", nullable = false, length = 100, unique = true)
    private String oldSlug;

    @Column(name = "new_slug", nullable = false, length = 100)
    private String newSlug;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
