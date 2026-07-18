package com.example.social_network.Service;

import com.example.social_network.Payload.Request.PostUserTagRequest;
import org.springframework.http.ResponseEntity;

public interface PostUserTagService {
    /** Gắn thẻ 1 user vào bài (taggerId = người thao tác, từ token). Người được gắn nhận thông báo TAG. */
    ResponseEntity<?> addTag(PostUserTagRequest dto, String taggerId, String ip);

    Object getByPost(String postId);

    /** Danh sách bài mà 1 user được gắn thẻ (hiển thị ở tab "Được gắn thẻ"). */
    Object getTaggedPosts(String userId);

    ResponseEntity<?> removeTag(PostUserTagRequest dto, String ip);
}
