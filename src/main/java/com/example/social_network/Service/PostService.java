package com.example.social_network.Service;

import com.example.social_network.models.Dto.DeleteDto;
import com.example.social_network.models.Dto.Posts.PostInsertDto;
import com.example.social_network.models.Dto.Posts.PostUpdateDto;
import org.springframework.http.ResponseEntity;

public interface PostService {
    Object getList(String id,String userId, String postId , int pageIdx, int pageSize);

    /**
     * Tạo bài viết kèm media. userId lấy từ token (KHÔNG nhận từ request).
     * Chạy trong @Transactional: rollback cả Post lẫn PostMedia nếu có lỗi.
     */
    ResponseEntity<?> insert(PostInsertDto dto, String userId, String ip);

    /** userId (chủ thể gọi API) lấy từ token — dùng để log và kiểm tra quyền sở hữu. */
    ResponseEntity<?> update(PostUpdateDto dto, String userId, String ip);

    /** userId (chủ thể gọi API) lấy từ token — dùng để log và kiểm tra quyền sở hữu. */
    ResponseEntity<?> delete(DeleteDto dto, String userId, String ip);
}
