package com.ihome24.ihome24.repository.product;

import com.ihome24.ihome24.entity.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT p FROM Product p LEFT JOIN p.category c " +
            "WHERE (:q IS NULL OR :q = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(COALESCE(p.sku, '')) LIKE LOWER(CONCAT('%', :q, '%'))) " +
            "AND ((:categoryId IS NOT NULL AND c.id = :categoryId) OR " +
            "(:categoryId IS NULL AND (:categoryName IS NULL OR :categoryName = '' OR LOWER(c.name) = LOWER(:categoryName)))) " +
            "AND (:stockInStock IS NULL OR " +
            "(:stockInStock = true AND p.stockQuantity > 0) OR " +
            "(:stockInStock = false AND (p.stockQuantity IS NULL OR p.stockQuantity <= 0))) " +
            "AND (:statusFilter IS NULL OR :statusFilter = '' OR " +
            "(:statusFilter = 'Published' AND p.isActive = true) OR " +
            "(:statusFilter = 'Inactive' AND (p.isActive = false OR p.isActive IS NULL)) OR " +
            "(:statusFilter = 'Scheduled' AND (COALESCE(p.status, '') IN ('scheduled', 'draft'))))")
    Page<Product> findAllWithSearch(@Param("q") String searchQuery,
                                    @Param("categoryId") Long categoryId,
                                    @Param("categoryName") String categoryName,
                                    @Param("stockInStock") Boolean stockInStock,
                                    @Param("statusFilter") String statusFilter,
                                    Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.category c LEFT JOIN FETCH c.parent LEFT JOIN FETCH p.images WHERE p.isActive = true ORDER BY p.createdAt DESC")
    List<Product> findByIsActiveTrue();
    
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category c LEFT JOIN FETCH c.parent LEFT JOIN FETCH p.images WHERE p.id = :id")
    Optional<Product> findByIdWithImages(Long id);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category c LEFT JOIN FETCH c.parent LEFT JOIN FETCH p.images WHERE p.id IN :ids AND p.isActive = true")
    List<Product> findByIdInAndIsActiveTrue(List<Long> ids);
}
