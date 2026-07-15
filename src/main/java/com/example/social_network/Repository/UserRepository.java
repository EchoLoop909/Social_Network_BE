package com.example.social_network.Repository;

import com.example.social_network.models.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    // Cập nhật khóa chính id_user (dùng khi đồng bộ: gán id_user = sub do Keycloak cấp).
    // Mỗi lần gọi là 1 transaction riêng để không giữ transaction dài khi lặp nhiều user.
    @Transactional
    @Modifying
    @Query(value = "UPDATE users SET id_user = :newId WHERE id_user = :oldId", nativeQuery = true)
    int updateUserId(@Param("newId") String newId, @Param("oldId") String oldId);
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    // Lọc theo id (chính xác) và/hoặc tìm theo tên (LIKE trên username/name/firstname/lastname).
    // Cả 2 tham số đều nhận null -> khi null thì bỏ qua điều kiện tương ứng. Ẩn user đã bị xóa mềm.
    @Query(
            value = "SELECT * FROM users WHERE (:userId IS NULL OR id_user = :userId) " +
                    "AND (:keyword IS NULL " +
                    "     OR username LIKE CONCAT('%', :keyword, '%') " +
                    "     OR name LIKE CONCAT('%', :keyword, '%') " +
                    "     OR firstname LIKE CONCAT('%', :keyword, '%') " +
                    "     OR lastname LIKE CONCAT('%', :keyword, '%')) " +
                    "AND deletion_date IS NULL",
            countQuery = "SELECT COUNT(*) FROM users WHERE (:userId IS NULL OR id_user = :userId) " +
                    "AND (:keyword IS NULL " +
                    "     OR username LIKE CONCAT('%', :keyword, '%') " +
                    "     OR name LIKE CONCAT('%', :keyword, '%') " +
                    "     OR firstname LIKE CONCAT('%', :keyword, '%') " +
                    "     OR lastname LIKE CONCAT('%', :keyword, '%')) " +
                    "AND deletion_date IS NULL",
            nativeQuery = true
    )
    Page<User> findUser(@Param("userId") String userId, @Param("keyword") String keyword, Pageable pageable);
}
