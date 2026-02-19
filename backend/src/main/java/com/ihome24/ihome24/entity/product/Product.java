package com.ihome24.ihome24.entity.product;

import com.ihome24.ihome24.entity.category.Category;
import com.ihome24.ihome24.entity.collection.Collection;
import com.ihome24.ihome24.entity.supplier.Supplier;
import com.ihome24.ihome24.entity.tag.Tag;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "old_price", precision = 10, scale = 2)
    private BigDecimal oldPrice;

    @Column(name = "sku", unique = true)
    private String sku;

    @Column(name = "brand")
    private String brand;

    @Column(name = "quantity_per_package")
    private Integer quantityPerPackage;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "image_url")
    private String imageUrl;

    @Column
    private String barcode;

    @Column(name = "tax_enabled")
    @Builder.Default
    private Boolean taxEnabled = false;

    @Column
    @Builder.Default
    private String status = "draft";

    @Column(name = "discounted_price", precision = 10, scale = 2)
    private BigDecimal discountedPrice;

    @Column(name = "characteristics", columnDefinition = "TEXT")
    private String characteristicsJson; // JSON строка для хранения характеристик

    @Column(name = "benefits", columnDefinition = "TEXT")
    private String benefitsJson; // JSON строка для хранения преимуществ

    @Column(name = "variants", columnDefinition = "TEXT")
    private String variantsJson; // JSON строка для хранения вариантов товара

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    private Collection collection;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "product_tags",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private List<Tag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
