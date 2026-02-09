package com.ihome24.ihome24.repository.order;

import com.ihome24.ihome24.entity.order.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(Long orderNumber);

    @EntityGraph(attributePaths = {"items"})
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    @EntityGraph(attributePaths = {"items"})
    @Query("SELECT o FROM Order o WHERE o.orderNumber = :orderNumber")
    Optional<Order> findByOrderNumberWithItems(@Param("orderNumber") Long orderNumber);

    @Query(value = "SELECT COALESCE(MAX(o.orderNumber), 0) FROM Order o")
    Long findMaxOrderNumber();

    @Query("SELECT o FROM Order o WHERE " +
           "(:searchQuery IS NULL OR :searchQuery = '' OR " +
           "LOWER(o.customer) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
           "LOWER(o.email) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
           "CAST(o.orderNumber AS string) LIKE CONCAT('%', :searchQuery, '%'))")
    Page<Order> findOrdersWithSearch(@Param("searchQuery") String searchQuery, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE " +
           "((:completed = true AND o.status = 'DELIVERED') OR (:completed = false AND o.status <> 'DELIVERED')) " +
           "AND (:searchQuery IS NULL OR :searchQuery = '' OR " +
           "LOWER(o.customer) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
           "LOWER(o.email) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
           "CAST(o.orderNumber AS string) LIKE CONCAT('%', :searchQuery, '%'))")
    Page<Order> findOrdersWithSearchAndCompleted(@Param("searchQuery") String searchQuery,
                                                 @Param("completed") boolean completed,
                                                 Pageable pageable);

    long countByPayment(Order.PaymentStatus payment);

    long countByStatus(Order.OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.spent), 0) FROM Order o WHERE o.status = 'DELIVERED'")
    BigDecimal sumSpentByDelivered();

    @Query("SELECT COUNT(DISTINCT o.email) FROM Order o")
    long countDistinctCustomers();

    List<Order> findTop10ByStatusOrderByCreatedAtDesc(Order.OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.status IN ('DISPATCHED', 'OUT_FOR_DELIVERY', 'READY_TO_PICKUP') ORDER BY o.createdAt DESC")
    List<Order> findInDeliveryOrderByCreatedAtDesc(Pageable pageable);
}