package com.example.social_network.Repository;

import com.example.social_network.models.Entity.User;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByUserId(@Param("id") Long id);

    Optional<User> findByUsername(String username);

    @Query(
            value = "SELECT * FROM users WHERE (:userId IS NULL OR id_user = :userId)",
            countQuery = "SELECT COUNT(*) FROM users WHERE (:userId IS NULL OR id_user = :userId)",
            nativeQuery = true
    )
    Page<User> findUser(@Param("userId") String userId, Pageable pageable);

    Optional<User> findByKeycloakId(String keycloackId);
}
