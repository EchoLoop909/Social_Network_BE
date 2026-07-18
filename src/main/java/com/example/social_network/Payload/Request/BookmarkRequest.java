package com.example.social_network.Payload.Request;

import lombok.Data;
import lombok.NoArgsConstructor;

/** Yêu cầu lưu / bỏ lưu 1 bài viết. postId bắt buộc; userId lấy từ token. */
@NoArgsConstructor
@Data
public class BookmarkRequest {
    private String postId;
}
