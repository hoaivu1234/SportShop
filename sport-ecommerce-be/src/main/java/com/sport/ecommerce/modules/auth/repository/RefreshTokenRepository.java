package com.sport.ecommerce.modules.auth.repository;

import com.sport.ecommerce.modules.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    /**
     * Bulk DELETE by token value — does NOT throw StaleStateException
     * when the row is already gone (idempotent, safe for concurrent calls).
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken r WHERE r.token = :token")
    void deleteByTokenValue(@Param("token") String token);
}
