package com.example.social_network.Repository;

import com.example.social_network.models.Entity.VideoView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoViewRepository extends JpaRepository<VideoView, String> {

    // Lượt xem của 1 user với 1 bài (dùng cho upsert).
    Optional<VideoView> findByUser_IdAndPost_Id(String userId, String postId);

    // Lấy [postId, watchedRatio] các video 1 user đã xem -> dựng vector sở thích.
    @Query("SELECT v.post.id, v.watchedRatio FROM VideoView v WHERE v.user.id = :userId")
    List<Object[]> findViewsByUser(@Param("userId") String userId);
}
