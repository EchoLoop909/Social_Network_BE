package com.example.social_network.Payload.Request;

import lombok.Data;

@Data
public class CommentRequest {

    // Bài viết được bình luận (bắt buộc)
    private String postId;

    // Nội dung bình luận (bắt buộc, tối đa 2000 ký tự)
    private String text;

    // Comment cha: null/rỗng = comment gốc; có giá trị = reply cho comment gốc đó
    private String parentCommentId;

    // id_user KHÔNG nhận từ request — lấy từ JWT subject ở tầng Controller/Service
}
