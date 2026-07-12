package com.example.social_network.Service;

import com.example.social_network.Payload.Request.AcceptFriendRequestDto;
import com.example.social_network.Payload.Request.FriendRequestDto;
import org.springframework.http.ResponseEntity;

public interface FollowershipService {

    ResponseEntity<?> sendFriendRequest(FriendRequestDto dto, String userId, String ip);

    /**
     * Chấp nhận lời mời kết bạn. userId (người chấp nhận A) lấy từ token = userChecked;
     * requesterId trong dto (người đã gửi B) = userFollower. Chỉ người NHẬN mới duyệt được.
     * Chuyển record PENDING -> ACCEPTED, set acceptedAt + updateTime.
     */
    ResponseEntity<?> acceptFriendRequest(AcceptFriendRequestDto dto, String userId, String ip);
}
