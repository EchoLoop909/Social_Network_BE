package com.example.social_network.Repository;

import com.example.social_network.models.Entity.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, String> {

    // Kiểm tra / lấy 1 bookmark theo cặp (user, post) — dùng cho toggle.
    Optional<Bookmark> findByUser_IdAndPost_Id(String userId, String postId);

    // Danh sách bài đã lưu của 1 user (mới lưu trước) — JOIN FETCH post để trả kèm bài.
    @Query(value = "SELECT b FROM Bookmark b JOIN FETCH b.post WHERE b.user.id = :userId ORDER BY b.createTime DESC",
            countQuery = "SELECT COUNT(b) FROM Bookmark b WHERE b.user.id = :userId")
    Page<Bookmark> findByUserOrderByNewest(@Param("userId") String userId, Pageable pageable);

    // Danh sách id bài mà user đã lưu (để FE tô nút "đã lưu").
    @Query("SELECT b.post.id FROM Bookmark b WHERE b.user.id = :userId")
    List<String> findPostIds(@Param("userId") String userId);
}
