package com.example.social_network.Controller;

import com.example.social_network.Payload.Request.AcceptFriendRequestDto;
import com.example.social_network.Payload.Request.FriendRequestDto;
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
}
