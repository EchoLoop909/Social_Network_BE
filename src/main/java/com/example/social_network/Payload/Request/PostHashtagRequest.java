package com.example.social_network.Payload.Request;

import lombok.Data;
import lombok.NoArgsConstructor;

/** Gắn / gỡ 1 hashtag khỏi 1 bài viết (khóa tổ hợp postId + hashtagId). */
@NoArgsConstructor
@Data
public class PostHashtagRequest {
    private String postId;
    private String hashtagId;
}
