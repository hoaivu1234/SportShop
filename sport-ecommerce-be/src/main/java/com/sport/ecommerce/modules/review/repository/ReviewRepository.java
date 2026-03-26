package com.sport.ecommerce.modules.review.repository;

import com.sport.ecommerce.modules.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query(
        value      = "SELECT r FROM Review r JOIN FETCH r.user WHERE r.product.id = :productId",
        countQuery = "SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId"
    )
    Page<Review> findByProductIdWithUser(@Param("productId") Long productId, Pageable pageable);

    boolean existsByProductIdAndUserId(Long productId, Long userId);

    Optional<Review> findByProductIdAndUserId(Long productId, Long userId);

    Page<Review> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT AVG(CAST(r.rating AS double)) FROM Review r WHERE r.product.id = :productId")
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    long countByProductId(Long productId);

    @Query("""
        SELECT r.rating, COUNT(r)
        FROM Review r
        WHERE r.product.id = :productId
        GROUP BY r.rating
        ORDER BY r.rating DESC
    """)
    List<Object[]> findRatingDistributionByProductId(@Param("productId") Long productId);
}
