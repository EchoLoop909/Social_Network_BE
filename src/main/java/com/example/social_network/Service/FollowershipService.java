package com.example.social_network.Service;

import com.example.social_network.Payload.Request.AcceptFriendRequestDto;
import com.example.social_network.Payload.Request.FriendRequestDto;
import com.example.social_network.Payload.Request.UnfriendDto;
import org.springframework.http.ResponseEntity;

public interface FollowershipService {

    ResponseEntity<?> sendFriendRequest(FriendRequestDto dto, String userId, String ip);

    ResponseEntity<?> acceptFriendRequest(AcceptFriendRequestDto dto, String userId, String ip);

    // A (userId từ token) hủy kết bạn/hủy theo dõi với B: xóa mọi quan hệ giữa 2 người (cả 2 chiều).
    ResponseEntity<?> unfriend(UnfriendDto dto, String userId, String ip);
}
