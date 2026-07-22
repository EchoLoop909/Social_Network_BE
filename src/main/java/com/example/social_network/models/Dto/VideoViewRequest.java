package com.example.social_network.models.Dto;

import lombok.Data;

/**
 * FE gửi tiến độ xem video lên BE (khi pause / ended / lướt qua).
 */
@Data
public class VideoViewRequest {
    private String postId;         // video nào
    private Integer watchedSeconds; // xem được bao nhiêu giây (vị trí xa nhất)
    private Integer duration;       // tổng thời lượng video (giây) — FE đọc từ thẻ <video>
}
