package com.example.social_network.Repository;

import com.example.social_network.models.Entity.PostMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostMediaRepository extends JpaRepository<PostMedia, String> {
    // Lấy media của 1 bài viết theo đúng thứ tự hiển thị (carousel)
    List<PostMedia> findByPost_IdOrderByDisplayOrderAsc(String postId);
}
