package com.sport.ecommerce.modules.wishlist.entity;

import com.sport.ecommerce.modules.product.entity.Product;
import com.sport.ecommerce.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "wishlist_items",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_wishlist_user_product",
        columnNames = {"user_id", "product_id"}
    ),
    indexes = {
        @Index(name = "idx_wishlist_user_id",    columnList = "user_id"),
        @Index(name = "idx_wishlist_product_id", columnList = "product_id")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Optional: the specific variant that was wishlisted. */
    private Long variantId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
