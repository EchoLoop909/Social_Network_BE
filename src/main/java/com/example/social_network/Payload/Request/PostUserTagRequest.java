package com.example.social_network.Payload.Request;

import lombok.Data;
import lombok.NoArgsConstructor;

/** Gắn thẻ (nhắc đến) 1 user vào 1 bài viết (khóa tổ hợp postId + userId). */
@NoArgsConstructor
@Data
public class PostUserTagRequest {
    private String postId;
    private String userId;
}
