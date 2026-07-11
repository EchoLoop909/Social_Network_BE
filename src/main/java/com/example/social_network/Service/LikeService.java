package com.example.social_network.Service;

import com.example.social_network.Payload.Request.LikeRequestDto;
import org.springframework.http.ResponseEntity;

public interface LikeService {

    // API 1: react (tạo mới hoặc đổi loại cảm xúc). userId lấy từ JWT subject.
    ResponseEntity<?> react(LikeRequestDto dto, String userId, String ip);

    // API 2: unlike (xoá cảm xúc của user hiện tại cho 1 đối tượng).
    ResponseEntity<?> unlike(LikeRequestDto dto, String userId, String ip);

    // API 3: danh sách người đã react cho 1 đối tượng (phân trang) + thống kê theo loại.
    Object getList(String targetType, String targetId, int pageIdx, int pageSize);

    // API 4: cảm xúc hiện tại của user đang đăng nhập cho 1 đối tượng (null nếu chưa react).
    Object getMyReaction(String targetType, String targetId, String userId);
}
