package com.example.social_network.Repository;

import com.example.social_network.models.Entity.Post;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post,String> {
//    Optional<Post> findByPostId(String postId);

    Optional<Post> findById(String id);
    @Query(
            value =
                    "SELECT * " +
                            "FROM posts p " +
                            "WHERE (:id IS NULL OR p.id_post = :id) " +
                            "  AND (:userId IS NULL OR p.id_user = :userId) " +
                            "  AND (:keyword IS NULL OR p.text LIKE CONCAT('%', :keyword, '%')) " +
                            "ORDER BY p.create_time DESC",
            countQuery =
                    "SELECT COUNT(*) " +
                            "FROM posts p " +
                            "WHERE (:id IS NULL OR p.id_post = :id) " +
                            "  AND (:userId IS NULL OR p.id_user = :userId) " +
                            "  AND (:keyword IS NULL OR p.text LIKE CONCAT('%', :keyword, '%'))",
            nativeQuery = true
    )
    Page<Post> findPosts(@Param("id") String id,
                         @Param("userId") String userId,
                         @Param("keyword") String keyword,
                         Pageable pageable);

}
