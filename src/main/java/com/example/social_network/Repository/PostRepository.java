package com.example.social_network.Repository;

import com.example.social_network.models.Entity.Post;
import com.example.social_network.models.Entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    /**
     * Feed cho người đang đăng nhập (viewerId): chỉ lấy bài mà người xem ĐƯỢC PHÉP thấy —
     * tác giả public (is_private = 0), HOẶC chính mình, HOẶC đã là bạn bè (ACCEPTED, xét cả 2 chiều).
     * Lọc ngay trong SQL để phân trang đúng.
     */
    @Query(
            value =
                    "SELECT p.* FROM posts p JOIN users u ON u.id_user = p.id_user " +
                            "WHERE ( u.is_private = 0 " +
                            "        OR p.id_user = :viewerId " +
                            "        OR EXISTS (SELECT 1 FROM followerships f WHERE f.status = 'ACCEPTED' " +
                            "             AND ((f.id_user_follower = :viewerId AND f.id_user_checked = p.id_user) " +
                            "               OR (f.id_user_follower = p.id_user AND f.id_user_checked = :viewerId))) ) " +
                            "  AND NOT EXISTS (SELECT 1 FROM followerships b WHERE b.status = 'BLOCKED' " +
                            "        AND ((b.id_user_follower = :viewerId AND b.id_user_checked = p.id_user) " +
                            "          OR (b.id_user_follower = p.id_user AND b.id_user_checked = :viewerId))) " +
                            "ORDER BY p.create_time DESC",
            countQuery =
                    "SELECT COUNT(*) FROM posts p JOIN users u ON u.id_user = p.id_user " +
                            "WHERE ( u.is_private = 0 " +
                            "        OR p.id_user = :viewerId " +
                            "        OR EXISTS (SELECT 1 FROM followerships f WHERE f.status = 'ACCEPTED' " +
                            "             AND ((f.id_user_follower = :viewerId AND f.id_user_checked = p.id_user) " +
                            "               OR (f.id_user_follower = p.id_user AND f.id_user_checked = :viewerId))) ) " +
                            "  AND NOT EXISTS (SELECT 1 FROM followerships b WHERE b.status = 'BLOCKED' " +
                            "        AND ((b.id_user_follower = :viewerId AND b.id_user_checked = p.id_user) " +
                            "          OR (b.id_user_follower = p.id_user AND b.id_user_checked = :viewerId)))",
            nativeQuery = true
    )
    Page<Post> findFeed(@Param("viewerId") String viewerId, Pageable pageable);

    // Danh sách user đã CHIA SẺ (repost) 1 bài viết (mới nhất trước).
    @Query("SELECT p.user FROM Post p WHERE p.originalPost.id = :postId AND p.isShared = true ORDER BY p.createTime DESC")
    List<User> findSharers(@org.springframework.data.repository.query.Param("postId") String postId);

}
