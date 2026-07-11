package com.example.social_network.Repository;

import com.example.social_network.models.Entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {

    // API 2: các comment GỐC của 1 bài viết (id_parent_comment IS NULL), mới nhất trước
    Page<Comment> findByPost_IdAndParentCommentIsNullOrderByCreateTimeDesc(String postId, Pageable pageable);

    // API 3: các reply của 1 comment gốc, cũ nhất trước (đúng thứ tự hội thoại)
    Page<Comment> findByParentComment_IdOrderByCreateTimeAsc(String parentCommentId, Pageable pageable);

    // API 2: đếm số reply của mỗi comment gốc để trả replyCount cho FE
    long countByParentComment_Id(String parentCommentId);
}
