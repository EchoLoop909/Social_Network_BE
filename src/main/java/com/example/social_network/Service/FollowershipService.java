package com.example.social_network.Service;

import com.example.social_network.Payload.Request.FriendRequestDto;
import org.springframework.http.ResponseEntity;

public interface FollowershipService {

    ResponseEntity<?> sendFriendRequest(FriendRequestDto dto, String userId, String ip);
}
