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

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.category c LEFT JOIN FETCH c.parent LEFT JOIN FETCH p.images ORDER BY p.createdAt DESC")
    List<Product> findAllWithCategory();

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.category c LEFT JOIN FETCH c.parent LEFT JOIN FETCH p.images WHERE p.isActive = true ORDER BY p.createdAt DESC")
    List<Product> findByIsActiveTrue();
    
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category c LEFT JOIN FETCH c.parent LEFT JOIN FETCH p.images WHERE p.id = :id")
    Optional<Product> findByIdWithImages(Long id);
}
