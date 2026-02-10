package com.ihome24.ihome24.repository.order;

import com.ihome24.ihome24.entity.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query(value = "SELECT oi.product_id, oi.product_name, SUM(oi.quantity), p.image_url " +
            "FROM order_items oi " +
            "JOIN orders o ON o.id = oi.order_id " +
            "LEFT JOIN products p ON p.id = oi.product_id " +
            "WHERE o.status = 'DELIVERED' " +
            "GROUP BY oi.product_id, oi.product_name, p.image_url " +
            "ORDER BY SUM(oi.quantity) DESC " +
            "LIMIT 5", nativeQuery = true)
    List<Object[]> findPopularProductsNative();
}
