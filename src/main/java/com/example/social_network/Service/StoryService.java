package com.example.social_network.Service;

import com.example.social_network.Payload.Request.StoryRequest;
import com.example.social_network.models.Dto.DeleteDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface StoryService {

    /** Đăng story mới bằng URL media có sẵn (dùng nội bộ). userId (chủ tin) lấy từ token. */
    ResponseEntity<?> createStory(StoryRequest dto, String userId, String ip);

    /** Đăng story bằng cách CHỌN FILE trực tiếp: BE upload Cloudinary rồi tạo story. */
    ResponseEntity<?> createStoryFromFile(MultipartFile file, String caption, Boolean isArchived,
                                          Integer expiresInHours, String metadata, String userId, String ip);

    /** Danh sách story CÒN HẠN của 1 user (ownerId) — dùng cho trang chủ (Highlight hết hạn KHÔNG hiện). */
    Object getStories(String ownerId, int pageIdx, int pageSize);

    /** Danh sách Highlight (isArchived=true) của 1 user — dùng cho trang cá nhân, giữ mãi. */
    Object getHighlights(String ownerId, int pageIdx, int pageSize);

    /** Bật/tắt Highlight cho 1 story. archived=null -> đảo trạng thái. Chỉ chủ tin. */
    ResponseEntity<?> setHighlight(String storyId, Boolean archived, String userId, String ip);

    /** Xóa story — chỉ chủ tin (userId từ token). Xóa cascade luôn story_views. */
    ResponseEntity<?> deleteStory(DeleteDto dto, String userId, String ip);
}
