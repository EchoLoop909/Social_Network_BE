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

    /** Danh sách story đang hiển thị (chưa hết hạn hoặc Highlight) của 1 user (ownerId). */
    Object getStories(String ownerId, int pageIdx, int pageSize);

    /** Xóa story — chỉ chủ tin (userId từ token). Xóa cascade luôn story_views. */
    ResponseEntity<?> deleteStory(DeleteDto dto, String userId, String ip);
}
