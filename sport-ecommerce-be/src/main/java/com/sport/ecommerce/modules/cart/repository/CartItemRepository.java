package com.sport.ecommerce.modules.cart.repository;

import com.sport.ecommerce.modules.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Loads all items for a cart in a single JOIN FETCH query to avoid N+1
     * when the service later accesses variant and product fields.
     */
    @Query("""
            SELECT ci FROM CartItem ci
            JOIN FETCH ci.productVariant pv
            JOIN FETCH pv.product
            WHERE ci.cart.id = :cartId
            ORDER BY ci.addedAt ASC
            """)
    List<CartItem> findByCartIdWithVariant(@Param("cartId") Long cartId);

    Optional<CartItem> findByCartIdAndProductVariantId(Long cartId, Long productVariantId);
}
