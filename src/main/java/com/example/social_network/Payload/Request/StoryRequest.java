package com.example.social_network.Payload.Request;

import lombok.Data;

/**
 * DTO đăng story. id_user KHÔNG nhận từ request — lấy từ JWT subject ở tầng Controller/Service.
 * created_at / expires_at (= created_at + 24h) do entity tự sinh trong @PrePersist.
 */
@Data
public class StoryRequest {

    private String mediaUrl;

    private String mediaType;

    private String caption;

    private Boolean isArchived;

    private String metadata;
}
