package com.example.social_network.Repository;

import com.example.social_network.models.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query(
            value = "SELECT * FROM users WHERE (:userId IS NULL OR id_user = :userId)",
            countQuery = "SELECT COUNT(*) FROM users WHERE (:userId IS NULL OR id_user = :userId)",
            nativeQuery = true
    )
    Page<User> findUser(@Param("userId") String userId, Pageable pageable);
}
