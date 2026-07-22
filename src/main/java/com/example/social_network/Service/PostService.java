package com.example.social_network.Service;

import com.example.social_network.models.Dto.DeleteDto;
import com.example.social_network.models.Dto.Posts.PostInsertDto;
import com.example.social_network.models.Dto.Posts.PostShareDto;
import com.example.social_network.models.Dto.Posts.PostUpdateDto;
import org.springframework.http.ResponseEntity;

public interface PostService {
    // viewerId = người xem hiện tại (lấy từ token) để kiểm soát quyền xem tài khoản riêng tư.
    Object getList(String id,String userId, String postId , String viewerId, int pageIdx, int pageSize);

    /**
     * Tạo bài viết kèm media. userId lấy từ token (KHÔNG nhận từ request).
     * Chạy trong @Transactional: rollback cả Post lẫn PostMedia nếu có lỗi.
     */
    ResponseEntity<?> insert(PostInsertDto dto, String userId, String ip);

    /**
     * Share (repost nội bộ) 1 bài viết. userId (người share) lấy từ token.
     * Chạy trong @Transactional: ghi 2 bảng (tạo Post share + tăng share_count bài gốc).
     */
    ResponseEntity<?> share(PostShareDto dto, String userId, String ip);

    /** userId (chủ thể gọi API) lấy từ token — dùng để log và kiểm tra quyền sở hữu. */
    ResponseEntity<?> update(PostUpdateDto dto, String userId, String ip);

    /** userId (chủ thể gọi API) lấy từ token — dùng để log và kiểm tra quyền sở hữu. */
    ResponseEntity<?> delete(DeleteDto dto, String userId, String ip);

    /** Danh sách user đã chia sẻ (repost) 1 bài viết. */
    Object getSharers(String postId);

    /** REELS: danh sách bài VIDEO người xem được phép thấy (phân trang, cùng format feed). */
    Object getReels(String viewerId, int pageIdx, int pageSize);
}
