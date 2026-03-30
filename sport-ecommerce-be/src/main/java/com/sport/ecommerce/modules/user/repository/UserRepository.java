package com.sport.ecommerce.modules.user.repository;

import com.sport.ecommerce.modules.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("""
       select u from User u
       left join fetch u.roles
       where u.email = :email
       """)
    Optional<User> findByEmailFetchRoles(String email);

    boolean existsByEmail(String email);

    Page<User> findByStatus(String status, Pageable pageable);

    @Query("""
        SELECT u FROM User u
        WHERE (:keyword IS NULL OR :keyword = ''
               OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.lastName)  LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.email)     LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:status IS NULL OR :status = '' OR u.status = :status)
        ORDER BY u.createdAt DESC
        """)
    Page<User> searchCustomers(@Param("keyword") String keyword,
                               @Param("status")  String status,
                               Pageable pageable);
}
