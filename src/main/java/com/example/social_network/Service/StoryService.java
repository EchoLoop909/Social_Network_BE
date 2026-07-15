package com.example.social_network.Service;

import com.example.social_network.Payload.Request.StoryRequest;
import com.example.social_network.models.Dto.DeleteDto;
import org.springframework.http.ResponseEntity;

public interface StoryService {

    /** Đăng story mới. userId (chủ tin) lấy từ token. */
    ResponseEntity<?> createStory(StoryRequest dto, String userId, String ip);

    /** Danh sách story đang hiển thị (chưa hết hạn hoặc Highlight) của 1 user (ownerId). */
    Object getStories(String ownerId, int pageIdx, int pageSize);

    /** Xóa story — chỉ chủ tin (userId từ token). Xóa cascade luôn story_views. */
    ResponseEntity<?> deleteStory(DeleteDto dto, String userId, String ip);
}
