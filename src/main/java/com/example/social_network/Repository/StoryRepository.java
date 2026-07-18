package com.example.social_network.Repository;

import com.example.social_network.models.Entity.Story;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StoryRepository extends JpaRepository<Story, String> {

    // Story đã hết hạn và KHÔNG phải Highlight -> dùng cho scheduler thu hồi.
    // Dùng deleteAll(list) ở tầng service để cascade xóa story_views (bulk DELETE JPQL không cascade).
    List<Story> findByExpiresAtBeforeAndIsArchivedFalse(LocalDateTime now);

    // Story còn "hiển thị" của 1 user: chưa hết hạn (expires_at > now) HOẶC là Highlight (is_archived = true).
    // Dùng JPQL vì điều kiện OR trên 2 field khác nhau, derived method không biểu diễn gọn được.
    @Query("SELECT s FROM Story s WHERE s.user.id = :userId " +
            "AND (s.expiresAt > :now OR s.isArchived = true) " +
            "ORDER BY s.createdAt DESC")
    Page<Story> findVisibleByUser(@Param("userId") String userId,
                                  @Param("now") LocalDateTime now,
                                  Pageable pageable);

    // HOME: chỉ story CÒN HẠN (expires_at > now) — Highlight hết hạn sẽ KHÔNG hiện ở trang chủ.
    Page<Story> findByUser_IdAndExpiresAtAfterOrderByCreatedAtDesc(String userId, LocalDateTime now, Pageable pageable);

    // PROFILE: chỉ story Highlight (is_archived = true) — giữ mãi bất kể hết hạn.
    Page<Story> findByUser_IdAndIsArchivedTrueOrderByCreatedAtDesc(String userId, Pageable pageable);
}
