package com.example.social_network.Repository;

import com.example.social_network.models.Entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    // List<Comment> findByPost_IdOrderByCreateTimeDesc(String postId);
    Page<Comment> findByPost_IdOrderByCreateTimeDesc(String postId, Pageable pageable);
}