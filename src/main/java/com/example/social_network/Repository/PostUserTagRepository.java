package com.example.social_network.Repository;

import com.example.social_network.models.Entity.Post;
import com.example.social_network.models.Entity.PostUserTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostUserTagRepository extends JpaRepository<PostUserTag, PostUserTag.PostUserTagId> {

    // Danh sách tag (người được nhắc) trong 1 bài
    List<PostUserTag> findByPost_Id(String postId);

    // Danh sách bài mà 1 user được gắn thẻ (mới nhất trước)
    @Query("SELECT t.post FROM PostUserTag t WHERE t.user.id = :userId ORDER BY t.post.createTime DESC")
    List<Post> findTaggedPosts(@Param("userId") String userId);
}
