package com.example.social_network.Service;

import org.springframework.http.ResponseEntity;

public interface StoryViewService {

    /** Ghi nhận 1 lượt xem story (idempotent theo cặp story+user). viewerId lấy từ token. */
    ResponseEntity<?> recordView(String storyId, String viewerId, String ip);

    /** Danh sách người đã xem 1 story (mới nhất trước). */
    Object getViewers(String storyId);
}
