package com.ihome24.ihome24.repository.product;

import com.ihome24.ihome24.entity.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    Optional<Product> findByBarcode(String barcode);

    boolean existsByBarcode(String barcode);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category ORDER BY p.createdAt DESC")
    List<Product> findAllWithCategory();
}
