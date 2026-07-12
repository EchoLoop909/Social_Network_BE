package com.example.social_network.Payload.Request;

import lombok.Data;

@Data
public class FriendRequestDto {
    // id của B — người NHẬN lời mời kết bạn. Người gửi (A) KHÔNG nhận từ request,
    // lấy từ JWT subject ở tầng Controller/Service.
    private String targetUserId;
}
