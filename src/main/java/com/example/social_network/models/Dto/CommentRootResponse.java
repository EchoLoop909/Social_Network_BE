package com.example.social_network.models.Dto;

import com.example.social_network.models.Entity.Comment;
import lombok.Data;

/**
 * Bọc 1 comment gốc kèm số lượng reply (replyCount) cho danh sách API 2.
 * Danh sách reply KHÔNG kèm ở đây — FE tải riêng qua API 3 (/comment/replies).
 */
@Data
public class CommentRootResponse {
    private Comment comment;
    private long replyCount;

    public CommentRootResponse(Comment comment, long replyCount) {
        this.comment = comment;
        this.replyCount = replyCount;
    }
}
