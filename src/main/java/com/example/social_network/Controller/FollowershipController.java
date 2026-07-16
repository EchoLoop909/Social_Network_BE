package com.example.social_network.Controller;

import com.example.social_network.Payload.Request.AcceptFriendRequestDto;
import com.example.social_network.Payload.Request.BlockUserDto;
import com.example.social_network.Payload.Request.FriendRequestDto;
import com.example.social_network.Payload.Request.UnfriendDto;
import com.example.social_network.Payload.Util.PathResources;
import com.example.social_network.Payload.Util.SecurityUtils;
import com.example.social_network.Service.FollowershipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(PathResources.FRIENTDSHIP)
public class FollowershipController {

    @Autowired
    private FollowershipService followershipService;

    // Gửi lời mời kết bạn. Người gửi (A) lấy từ token (KHÔNG nhận từ request).
    @PostMapping(PathResources.SENDFRIENDREQUEST)
    public ResponseEntity<?> sendFriendRequest(@RequestBody FriendRequestDto dto,
                                               HttpServletRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        return followershipService.sendFriendRequest(dto, userId, request.getRemoteAddr());
    }

    // Chấp nhận lời mời kết bạn. Người chấp nhận (A) lấy từ token (KHÔNG nhận từ request).
    @PostMapping(PathResources.ACCEPFRIENDREQUEST)
    public ResponseEntity<?> acceptFriendRequest(@RequestBody AcceptFriendRequestDto dto,
                                                 HttpServletRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        return followershipService.acceptFriendRequest(dto, userId, request.getRemoteAddr());
    }

    // Hủy kết bạn/hủy theo dõi. Người thực hiện (A) lấy từ token; xóa quan hệ 2 chiều với B.
    @PostMapping(PathResources.UNFRIEND)
    public ResponseEntity<?> unfriend(@RequestBody UnfriendDto dto,
                                      HttpServletRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        return followershipService.unfriend(dto, userId, request.getRemoteAddr());
    }

    // Danh sách bạn bè (ACCEPTED). Không truyền userId -> của người đang đăng nhập; có userId -> của user đó (xem trang cá nhân người khác).
    @GetMapping(PathResources.FRIENDS)
    public ResponseEntity<?> getFriends(@RequestParam(required = false) String userId) {
        String target = (userId != null && !userId.trim().isEmpty()) ? userId.trim() : SecurityUtils.getCurrentUserId();
        return followershipService.getFriends(target);
    }

    // Danh sách lời mời kết bạn ĐẾN người đang đăng nhập.
    @GetMapping(PathResources.FRIENDREQUESTS)
    public ResponseEntity<?> getFriendRequests() {
        return followershipService.getFriendRequests(SecurityUtils.getCurrentUserId());
    }

    // Danh sách lời mời ĐÃ GỬI ĐI (đang theo dõi). Không truyền userId -> của người đang đăng nhập; có userId -> của user đó.
    @GetMapping(PathResources.SENTREQUESTS)
    public ResponseEntity<?> getSentRequests(@RequestParam(required = false) String userId) {
        String target = (userId != null && !userId.trim().isEmpty()) ? userId.trim() : SecurityUtils.getCurrentUserId();
        return followershipService.getSentRequests(target);
    }

    // Gợi ý kết bạn cho người đang đăng nhập.
    @GetMapping(PathResources.FRIENDSUGGESTIONS)
    public ResponseEntity<?> getSuggestions() {
        return followershipService.getSuggestions(SecurityUtils.getCurrentUserId());
    }

    // Từ chối lời mời kết bạn. Người từ chối lấy từ token; xóa bản ghi requester -> mình.
    @PostMapping(PathResources.REJECTFRIENDREQUEST)
    public ResponseEntity<?> rejectFriendRequest(@RequestBody AcceptFriendRequestDto dto,
                                                 HttpServletRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        return followershipService.rejectFriendRequest(dto, userId, request.getRemoteAddr());
    }

    // Chặn user. Người chặn lấy từ token.
    @PostMapping(PathResources.BLOCKUSER)
    public ResponseEntity<?> blockUser(@RequestBody BlockUserDto dto,
                                       HttpServletRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        return followershipService.blockUser(dto, userId, request.getRemoteAddr());
    }

    // Bỏ chặn user. Người bỏ chặn lấy từ token.
    @PostMapping(PathResources.UNBLOCKUSER)
    public ResponseEntity<?> unblockUser(@RequestBody BlockUserDto dto,
                                         HttpServletRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        return followershipService.unblockUser(dto, userId, request.getRemoteAddr());
    }

    // Danh sách user mình đã chặn.
    @GetMapping(PathResources.BLOCKEDLIST)
    public ResponseEntity<?> getBlockedUsers() {
        return followershipService.getBlockedUsers(SecurityUtils.getCurrentUserId());
    }
}
