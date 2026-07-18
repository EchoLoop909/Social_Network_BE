package com.example.social_network.Repository;

import com.example.social_network.models.Entity.PostHashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostHashtagRepository extends JpaRepository<PostHashtag, PostHashtag.PostHashtagId> {

    // Các liên kết hashtag của 1 bài (JOIN FETCH để lấy kèm hashtag)
    List<PostHashtag> findByPost_Id(String postId);
}
