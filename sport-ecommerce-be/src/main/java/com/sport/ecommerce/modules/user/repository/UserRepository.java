package com.sport.ecommerce.modules.user.repository;

import com.sport.ecommerce.modules.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}
