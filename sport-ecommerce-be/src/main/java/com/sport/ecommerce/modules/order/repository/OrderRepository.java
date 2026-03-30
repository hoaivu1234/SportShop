package com.sport.ecommerce.modules.order.repository;

import com.sport.ecommerce.modules.order.entity.Order;
import com.sport.ecommerce.modules.order.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    /** User's own orders, most-recent first. */
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /** Admin: filter by optional status. */
    @Query("""
            SELECT o FROM Order o
            WHERE (:status IS NULL OR o.status = :status)
            ORDER BY o.createdAt DESC
            """)
    Page<Order> findAllByStatus(@Param("status") OrderStatus status, Pageable pageable);

    /** Load order with items and variant in one query to avoid N+1. */
    @Query("""
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.items i
            LEFT JOIN FETCH i.productVariant
            WHERE o.id = :id
            """)
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    /** Same but looked up by orderNumber (used on confirmation page). */
    @Query("""
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.items i
            LEFT JOIN FETCH i.productVariant
            WHERE o.orderNumber = :orderNumber
            """)
    Optional<Order> findByOrderNumberWithItems(@Param("orderNumber") String orderNumber);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.user.id = :userId")
    BigDecimal sumTotalPriceByUserId(@Param("userId") Long userId);

    List<Order> findTop3ByUserIdOrderByCreatedAtDesc(Long userId);
}
