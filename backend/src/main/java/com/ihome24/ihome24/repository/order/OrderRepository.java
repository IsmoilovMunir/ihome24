package com.ihome24.ihome24.repository.order;

import com.ihome24.ihome24.entity.order.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(Long orderNumber);

    @Query("SELECT o FROM Order o WHERE " +
           "(:searchQuery IS NULL OR :searchQuery = '' OR " +
           "LOWER(o.customer) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
           "LOWER(o.email) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
           "CAST(o.orderNumber AS string) LIKE CONCAT('%', :searchQuery, '%'))")
    Page<Order> findOrdersWithSearch(@Param("searchQuery") String searchQuery, Pageable pageable);
}