package com.example.social_network.Payload.Request;

import lombok.Data;

@Data
public class UnfriendDto {
    // id của B — người mà A (lấy từ JWT) muốn hủy kết bạn/hủy theo dõi.
    // Người thực hiện (A) KHÔNG nhận từ request, lấy từ JWT subject ở tầng Controller.
    private String targetUserId;
}
