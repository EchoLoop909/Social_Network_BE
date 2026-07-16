package com.example.social_network.Payload.Request;

import lombok.Data;

/**
 * DTO ghi nhận 1 lượt xem story. id_user (người xem) KHÔNG nhận từ body — lấy từ JWT subject.
 */
@Data
public class StoryViewRequest {
    private String storyId;
}
