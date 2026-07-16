package com.example.social_network.Payload.Request;

import lombok.Data;


@Data
public class StoryRequest {

    private String mediaUrl;

    private String mediaType;

    private String caption;

    private Boolean isArchived;

    // Số giờ story tồn tại trước khi hết hạn. Null -> mặc định 24h. Hợp lệ 1..168 (7 ngày).
    private Integer expiresInHours;

    // JSON overlay: nhạc, vị trí caption, sticker... (toạ độ chuẩn hoá 0..1). Null nếu không có.
    private String metadata;
}
