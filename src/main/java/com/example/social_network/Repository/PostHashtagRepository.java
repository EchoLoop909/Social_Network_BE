package com.example.social_network.Repository;

import com.example.social_network.models.Entity.PostHashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostHashtagRepository extends JpaRepository<PostHashtag, PostHashtag.PostHashtagId> {

    // Các liên kết hashtag của 1 bài (JOIN FETCH để lấy kèm hashtag)
    List<PostHashtag> findByPost_Id(String postId);

    // Lấy THẲNG tên hashtag của 1 bài (tránh lazy-load Hashtag ngoài transaction)
    @Query("SELECT h.name FROM PostHashtag ph JOIN ph.hashtag h WHERE ph.post.id = :postId")
    List<String> findHashtagNamesByPostId(@Param("postId") String postId);
}
