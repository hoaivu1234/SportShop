package com.sport.ecommerce.modules.wishlist.repository;

import com.sport.ecommerce.modules.wishlist.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {

    @Query("SELECT w FROM WishlistItem w JOIN FETCH w.product WHERE w.user.id = :userId ORDER BY w.createdAt DESC")
    List<WishlistItem> findByUserIdWithProduct(@Param("userId") Long userId);

    Optional<WishlistItem> findByUserIdAndProductId(Long userId, Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserId(Long userId);
}
