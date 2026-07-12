package com.example.social_network.Payload.Request;

import lombok.Data;

@Data
public class AcceptFriendRequestDto {
    // id của B — người ĐÃ GỬI lời mời (userFollower). Người chấp nhận (A) KHÔNG nhận từ
    // request, lấy từ JWT subject ở tầng Controller/Service.
    private String requesterId;
}
