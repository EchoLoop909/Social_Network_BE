package com.example.social_network.Controller;

import com.example.social_network.Payload.Request.LikeRequestDto;
import com.example.social_network.Payload.Util.PathResources;
import com.example.social_network.Service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(PathResources.LIKE)
public class LikeController {

    @Autowired
    private LikeService likeService;

    // API 1: react (tạo mới hoặc đổi loại cảm xúc). id_user lấy từ JWT subject.
    @PostMapping(PathResources.REACT)
    public ResponseEntity<?> react(@RequestBody LikeRequestDto dto,
                                   @AuthenticationPrincipal Jwt jwt,
                                   HttpServletRequest request) {
        return likeService.react(dto, jwt.getSubject(), request.getRemoteAddr());
    }

    // API 2: unlike (xoá cảm xúc của user hiện tại cho 1 đối tượng).
    @DeleteMapping(PathResources.UNLIKE)
    public ResponseEntity<?> unlike(@RequestBody LikeRequestDto dto,
                                    @AuthenticationPrincipal Jwt jwt,
                                    HttpServletRequest request) {
        return likeService.unlike(dto, jwt.getSubject(), request.getRemoteAddr());
    }

    // API 3: danh sách người đã react cho 1 đối tượng (phân trang) + thống kê theo loại.
    @GetMapping(PathResources.LIST)
    public Object getList(@RequestParam String targetType,
                          @RequestParam String targetId,
                          @RequestParam(defaultValue = "1") int pageIdx,
                          @RequestParam(defaultValue = "10") int pageSize) {
        return likeService.getList(targetType, targetId, pageIdx - 1, pageSize);
    }

    // API 4: cảm xúc hiện tại của user đang đăng nhập cho 1 đối tượng.
    @GetMapping(PathResources.MY_REACTION)
    public Object getMyReaction(@RequestParam String targetType,
                                @RequestParam String targetId,
                                @AuthenticationPrincipal Jwt jwt) {
        return likeService.getMyReaction(targetType, targetId, jwt.getSubject());
    }
}
