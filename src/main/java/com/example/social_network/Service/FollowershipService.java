package com.example.social_network.Service;

import com.example.social_network.Payload.Request.AcceptFriendRequestDto;
import com.example.social_network.Payload.Request.BlockUserDto;
import com.example.social_network.Payload.Request.FriendRequestDto;
import com.example.social_network.Payload.Request.UnfriendDto;
import org.springframework.http.ResponseEntity;

public interface FollowershipService {

    ResponseEntity<?> sendFriendRequest(FriendRequestDto dto, String userId, String ip);

    ResponseEntity<?> acceptFriendRequest(AcceptFriendRequestDto dto, String userId, String ip);

    // A (userId từ token) hủy kết bạn/hủy theo dõi với B: xóa mọi quan hệ giữa 2 người (cả 2 chiều).
    ResponseEntity<?> unfriend(UnfriendDto dto, String userId, String ip);

    // Danh sách bạn bè (quan hệ ACCEPTED) của userId.
    ResponseEntity<?> getFriends(String userId);

    // Danh sách lời mời kết bạn ĐẾN userId (đang chờ xử lý).
    ResponseEntity<?> getFriendRequests(String userId);

    // Gợi ý kết bạn: các user chưa có quan hệ nào với userId.
    ResponseEntity<?> getSuggestions(String userId);

    // Từ chối lời mời từ requesterId (xóa bản ghi requester -> userId). userId lấy từ token.
    ResponseEntity<?> rejectFriendRequest(AcceptFriendRequestDto dto, String userId, String ip);

    // A (userId) chặn B: xóa mọi quan hệ cũ rồi tạo bản ghi BLOCKED (A -> B).
    ResponseEntity<?> blockUser(BlockUserDto dto, String userId, String ip);

    // A (userId) bỏ chặn B: xóa bản ghi BLOCKED (A -> B).
    ResponseEntity<?> unblockUser(BlockUserDto dto, String userId, String ip);

    // Danh sách user mà userId đã chặn.
    ResponseEntity<?> getBlockedUsers(String userId);
}
