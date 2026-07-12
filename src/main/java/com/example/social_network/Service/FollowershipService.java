package com.example.social_network.Service;

import com.example.social_network.Payload.Request.FriendRequestDto;
import org.springframework.http.ResponseEntity;

public interface FollowershipService {

    /**
     * Gửi lời mời kết bạn. userId (người gửi A) lấy từ token — lưu vào userFollower;
     * targetUserId trong dto (người nhận B) lưu vào userChecked; status = PENDING.
     */
    ResponseEntity<?> sendFriendRequest(FriendRequestDto dto, String userId, String ip);
}
